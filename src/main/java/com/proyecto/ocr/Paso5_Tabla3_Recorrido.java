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

// Clase especializada en la sección de recorrido y destinos
public class Paso5_Tabla3_Recorrido {

    public Map<String, String> extraerRecorrido(File archivoPdfEntrada) {
        Map<String, String> resultados = new LinkedHashMap<>();
        
        String[] anclas = {
            "PUNTO DE PARTIDA",
            "DESTINO(S) INMEDIATO (S)",
            "DESTINO FINAL ENTREGA DE PAQUETES"
        };

        try (PDDocument documento = PDDocument.load(archivoPdfEntrada)) {
            ObjectExtractor extractor = new ObjectExtractor(documento);
            SpreadsheetExtractionAlgorithm spreadsheet = new SpreadsheetExtractionAlgorithm();
            PageIterator it = extractor.extract();
            
            List<Table> todasLasTablas = new ArrayList<>();
            while (it.hasNext()) {
                Page pagina = it.next();
                // Ampliamos un poco más (65%) para capturar el último renglón antes del mapa
                Page areaRecorrido = pagina.getArea((float)(pagina.getHeight() * 0.25), 0f, (float)(pagina.getHeight() * 0.65), (float)pagina.getWidth());
                todasLasTablas.addAll(spreadsheet.extract(areaRecorrido));
            }
            extractor.close();

            for (String ancla : anclas) {
                String valor = buscarValorVertical(ancla, todasLasTablas);
                resultados.put(ancla, valor);
            }

        } catch (Exception e) {
            System.err.println("Error en Tabla 3: " + e.getMessage());
        }
        return resultados;
    }

    @SuppressWarnings("rawtypes")
    private String buscarValorVertical(String ancla, List<Table> tablas) {
        for (Table tabla : tablas) {
            int indiceFilaAncla = -1;
            RectangularTextContainer celdaAncla = null;

            int r = 0;
            for (List<RectangularTextContainer> fila : tabla.getRows()) {
                for (RectangularTextContainer celda : fila) {
                    String texto = celda.getText().toUpperCase();
                    // Coincidencia elástica para anclas largas
                    boolean coincide = false;
                    if (ancla.contains("PARTIDA")) coincide = texto.contains("PARTIDA");
                    else if (ancla.contains("INMEDIATO")) coincide = texto.contains("INMEDIATO");
                    else if (ancla.contains("DESTINO FINAL")) coincide = texto.contains("DESTINO") && texto.contains("FINAL");
                    else coincide = texto.contains(ancla.toUpperCase());

                    if (coincide) {
                        celdaAncla = celda;
                        indiceFilaAncla = r;
                        break;
                    }
                }
                if (celdaAncla != null) break;
                r++;
            }

            if (celdaAncla != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = indiceFilaAncla + 1; i < tabla.getRows().size(); i++) {
                    for (RectangularTextContainer celdaDeAbajo : tabla.getRows().get(i)) {
                        double centroX = celdaDeAbajo.getX() + (celdaDeAbajo.getWidth() / 2.0);
                        if (centroX >= celdaAncla.getX() && centroX <= (celdaAncla.getX() + celdaAncla.getWidth())) {
                            String txt = celdaDeAbajo.getText().trim();
                            if (!txt.isEmpty() && !txt.toUpperCase().contains(ancla.toUpperCase())) {
                                sb.append(txt).append(" ");
                            }
                        }
                    }
                }
                return sb.toString().replace("\r", " ").replace("\n", " ").replaceAll(" +", " ").trim();
            }
        }
        return "No detectado";
    }
}
