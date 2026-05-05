package com.proyecto.ocr;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import technology.tabula.RectangularTextContainer;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

// Clase especializada en la cabecera (Lógica de Anclas - Versión Robusta)
public class Paso5_Tabla1_Cabecera {

    public Map<String, String> extraerCabecera(File archivoPdfEntrada) {
        Map<String, String> resultados = new LinkedHashMap<>();
        
        String[] anclas = {
            "DISTRITO FEDERAL",
            "DISTRITO JUDICIAL",
            "MUNICIPIO:",
            "TIPO Y NÚMERO DE MECANISMO"
        };

        try (PDDocument documento = PDDocument.load(archivoPdfEntrada)) {
            ObjectExtractor extractor = new ObjectExtractor(documento);
            SpreadsheetExtractionAlgorithm spreadsheet = new SpreadsheetExtractionAlgorithm();
            PageIterator it = extractor.extract();
            
            List<Table> todasLasTablas = new ArrayList<>();
            while (it.hasNext()) {
                Page pagina = it.next();
                Page areaSuperior = pagina.getArea(0f, 0f, (float)(pagina.getHeight() * 0.25), (float)pagina.getWidth());
                todasLasTablas.addAll(spreadsheet.extract(areaSuperior));
            }
            extractor.close();

            for (String ancla : anclas) {
                String valor = buscarValorAncla(ancla, todasLasTablas);
                resultados.put(ancla, valor);
            }

        } catch (Exception e) {
            System.err.println("Error en Tabla 1: " + e.getMessage());
        }
        return resultados;
    }

    @SuppressWarnings("rawtypes")
    private String buscarValorAncla(String ancla, List<Table> tablas) {
        String anclaMayus = ancla.toUpperCase();
        
        for (Table tabla : tablas) {
            List<List<RectangularTextContainer>> filas = tabla.getRows();
            for (int r = 0; r < filas.size(); r++) {
                List<RectangularTextContainer> fila = filas.get(r);
                
                for (int c = 0; c < fila.size(); c++) {
                    String textoCelda = fila.get(c).getText().toUpperCase().replace("\r", " ").replace("\n", " ").trim();
                    
                    boolean coincide = false;
                    if (anclaMayus.contains("FEDERAL")) coincide = textoCelda.contains("FEDERAL") || (textoCelda.contains("ISTRITO") && !anclaMayus.contains("JUDICIAL"));
                    else if (anclaMayus.contains("JUDICIAL")) coincide = textoCelda.contains("UDICIAL");
                    else if (anclaMayus.contains("MUNICIPIO")) coincide = textoCelda.contains("MUNICIPIO");
                    else if (anclaMayus.contains("MECANISMO")) coincide = textoCelda.contains("MECANISMO") || textoCelda.contains("TIPO");
                    else coincide = textoCelda.contains(anclaMayus);

                    if (coincide) {
                        StringBuilder sb = new StringBuilder();
                        RectangularTextContainer celdaDatoInicial = null;

                        // 1. Buscar el primer dato a la derecha
                        for (int j = c + 1; j < fila.size(); j++) {
                            String txt = fila.get(j).getText().trim();
                            if (!txt.isEmpty()) {
                                sb.append(txt).append(" ");
                                celdaDatoInicial = fila.get(j);
                                break;
                            }
                        }

                        // 2. Multilínea inteligente (coordenadas X)
                        if (celdaDatoInicial != null) {
                            double xIni = celdaDatoInicial.getLeft();
                            double xFin = celdaDatoInicial.getRight();
                            
                            for (int i = r + 1; i < filas.size(); i++) {
                                for (RectangularTextContainer celdaSig : filas.get(i)) {
                                    double cX = celdaSig.getLeft() + (celdaSig.getWidth() / 2.0);
                                    // Si la celda de abajo está alineada horizontalmente con el dato
                                    if (cX >= xIni && cX <= xFin) {
                                        String txtAbajo = celdaSig.getText().trim();
                                        // Si no está vacío y no parece un título rosa nuevo
                                        if (!txtAbajo.isEmpty() && !txtAbajo.equals(txtAbajo.toUpperCase())) {
                                            sb.append(txtAbajo).append(" ");
                                        }
                                    }
                                }
                            }
                        }

                        if (sb.length() > 0) {
                            return sb.toString().replace("\r", " ").replace("\n", " ").replaceAll(" +", " ").trim();
                        }
                    }
                }
            }
        }
        return "No detectado";
    }
}
