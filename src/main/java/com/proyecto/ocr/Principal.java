// Paquete principal donde se alojan todas nuestras clases
package com.proyecto.ocr;

// Importaciones de Java para manejo de archivos y listas
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

// Clase orquestadora que contiene el método main
public class Principal {

    // Método principal estático, punto de entrada de cualquier programa Java
    public static void main(String[] args) {
        // 1. Configuración de entrada
        String rutaPdfEntrada = "a.pdf";
        File archivoPdf = new File(rutaPdfEntrada);

        if (!archivoPdf.exists()) {
            System.out.println("El archivo PDF no existe: " + archivoPdf.getAbsolutePath());
            return;
        }

        // 2. Crear carpeta de resultados específica para este documento
        // Esto crea "resultados/documento/"
        String nombreSinExt = archivoPdf.getName().replaceFirst("[.][^.]+$", "");
        String carpetaDestino = "resultados/" + nombreSinExt;
        File dirDestino = new File(carpetaDestino);
        if (!dirDestino.exists()) {
            dirDestino.mkdirs();
        }

        String rutaTxtSalida = carpetaDestino + "/resultado_ocr.txt";
        String rutaJsonSalida = carpetaDestino + "/tablas_estructuradas.json";

        // 3. Orquestación del flujo
        try {
            System.out.println("Iniciando procesamiento de: " + archivoPdf.getName());
            System.out.println("Los resultados se guardarán en: " + dirDestino.getAbsolutePath());

            // --- FLUJO 1: OCR ---
            Paso1_LectorDocumento lector = new Paso1_LectorDocumento();
            Paso2_ProcesadorImagen procesador = new Paso2_ProcesadorImagen();
            Paso3_AnalizadorTexto analizador = new Paso3_AnalizadorTexto();
            Paso4_EscritorArchivo escritor = new Paso4_EscritorArchivo();

            // Pasamos carpetaDestino para que los recortes base se guarden ahí dentro
            List<File> imagenes = lector.convertirPdfAImagenes(archivoPdf, carpetaDestino);

            for (int i = 0; i < imagenes.size(); i++) {
                System.out.println("Procesando página " + (i + 1) + " con OCR...");
                File imgPagina = imagenes.get(i);
                // Ahora le pasamos la carpeta destino para que guarde ahí la imagen limpia
                File imgLimpia = procesador.limpiarImagen(imgPagina, carpetaDestino);
                String texto = analizador.extraerTexto(imgLimpia);
                
                escritor.escribirResultado(rutaTxtSalida, i + 1, texto, (i == 0));
            }

            // --- FLUJO 2: TABLAS (MODULAR) ---
            System.out.println("\n[Paso 5] Iniciando extracción modular por tablas...");
            Map<String, Object> jsonFinal = new LinkedHashMap<>();
            
            Paso5_Tabla1_Cabecera t1 = new Paso5_Tabla1_Cabecera();
            jsonFinal.put("Tabla 1", t1.extraerCabecera(archivoPdf));
            
            Paso5_Tabla2_Eleccion t2 = new Paso5_Tabla2_Eleccion();
            jsonFinal.put("Tabla 2", t2.extraerElecciones(archivoPdf));
            
            Paso5_Tabla3_Recorrido t3 = new Paso5_Tabla3_Recorrido();
            jsonFinal.put("Tabla 3", t3.extraerRecorrido(archivoPdf));
            
            guardarJsonUnificado(jsonFinal, rutaJsonSalida);

            System.out.println("\n¡Procesamiento finalizado con éxito!");
            System.out.println("-> JSON: " + rutaJsonSalida);

        } catch (Exception e) {
            System.err.println("Ocurrió un error en el flujo de ejecución principal:");
            e.printStackTrace();
        }
    }

    // Método auxiliar para guardar el mapa final en un archivo JSON
    private static void guardarJsonUnificado(java.util.Map<String, Object> datos, String ruta) throws java.io.IOException {
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
        try (java.io.FileWriter writer = new java.io.FileWriter(ruta)) {
            gson.toJson(datos, writer);
        }
    }
}
