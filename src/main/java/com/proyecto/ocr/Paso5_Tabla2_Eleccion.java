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

// Clase especializada en la sección de elecciones
public class Paso5_Tabla2_Eleccion {

    public Map<String, String> extraerElecciones(File archivoPdfEntrada) {
        Map<String, String> resultados = new LinkedHashMap<>();
        
        String[] anclas = {
            "TIPO DE ELECCIÓN",
            "NÚMERO Y TIPO DE CASILLAS",
            "PAQUETES POR TIPO DE ELECCIÓN"
        };

        try (PDDocument documento = PDDocument.load(archivoPdfEntrada)) {
            ObjectExtractor extractor = new ObjectExtractor(documento);
            SpreadsheetExtractionAlgorithm spreadsheet = new SpreadsheetExtractionAlgorithm();
            PageIterator it = extractor.extract();
            
            List<Table> todasLasTablas = new ArrayList<>();
            while (it.hasNext()) {
                Page pagina = it.next();
                // Esta sección suele estar entre el 20% y 55% de la hoja
                Page areaMedia = pagina.getArea((float)(pagina.getHeight() * 0.20), 0f, (float)(pagina.getHeight() * 0.55), (float)pagina.getWidth());
                todasLasTablas.addAll(spreadsheet.extract(areaMedia));
            }
            extractor.close();

            for (String ancla : anclas) {
                String valor = buscarValorVertical(ancla, todasLasTablas);
                resultados.put(ancla, valor);
            }

        } catch (Exception e) {
            System.err.println("Error en Tabla 2: " + e.getMessage());
        }
        return resultados;
    }

    @SuppressWarnings("rawtypes")
    private String buscarValorVertical(String ancla, List<Table> tablas) {
        for (Table tabla : tablas) {
            int indiceFilaAncla = -1;
            int indiceColAncla = -1;
            RectangularTextContainer celdaAncla = null;

            int r = 0;
            for (List<RectangularTextContainer> fila : tabla.getRows()) {
                int c = 0;
                for (RectangularTextContainer celda : fila) {
                    if (celda.getText().toUpperCase().contains(ancla.toUpperCase())) {
                        celdaAncla = celda;
                        indiceFilaAncla = r;
                        indiceColAncla = c;
                        break;
                    }
                    c++;
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
                            sb.append(celdaDeAbajo.getText().trim()).append(" ");
                        }
                    }
                }
                return sb.toString().replace("\r", " ").replace("\n", " ").replaceAll(" +", " ").trim();
            }
        }
        return "No detectado";
    }
}
