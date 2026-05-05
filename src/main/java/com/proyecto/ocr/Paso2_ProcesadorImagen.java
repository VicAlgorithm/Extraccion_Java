// Paquete donde se encuentra la clase
package com.proyecto.ocr;

// Importaciones de JavaCV y OpenCV para manipular matrices de imágenes y aplicar filtros
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;

// Importación para el manejo de archivos
import java.io.File;

// Definición de la clase pública para procesar y limpiar las imágenes
public class Paso2_ProcesadorImagen {

    // Método que recibe un archivo de imagen original y la carpeta donde debe guardarse
    public File limpiarImagen(File imagenOriginal, String carpetaDestino) {
        // Lee la imagen original desde su ruta absoluta y la convierte en una matriz (Mat) de OpenCV
        Mat matrizImagen = opencv_imgcodecs.imread(imagenOriginal.getAbsolutePath());
        
        // Crea una matriz vacía para almacenar las versiones procesadas
        Mat imagenGris = new Mat();
        Mat imagenDesenfoque = new Mat();
        Mat imagenBinarizada = new Mat();
        
        // Aplicar filtros: Grises -> Desenfoque -> Binarización
        opencv_imgproc.cvtColor(matrizImagen, imagenGris, opencv_imgproc.COLOR_BGR2GRAY);
        opencv_imgproc.GaussianBlur(imagenGris, imagenDesenfoque, new Size(3, 3), 0);
        opencv_imgproc.threshold(imagenDesenfoque, imagenBinarizada, 0, 255, opencv_imgproc.THRESH_BINARY | opencv_imgproc.THRESH_OTSU);
        
        File imagenProcesada = null;
        try {
            // Extraer el número de página del nombre original (ej: "1_imagen_pdf_...")
            String nombreOriginal = imagenOriginal.getName();
            String numeroPagina = nombreOriginal.split("_")[0];
            
            // Construir la ruta final: carpetaDestino/2_imagen_limpia_pagX.png
            String nombreImagenLimpia = carpetaDestino + "/2_imagen_limpia_pag" + numeroPagina + ".png";
            
            imagenProcesada = new File(nombreImagenLimpia);
            // Escribir la imagen en el disco
            opencv_imgcodecs.imwrite(imagenProcesada.getAbsolutePath(), imagenBinarizada);
        } catch (Exception e) {
            System.err.println("Error al procesar y guardar la imagen: " + e.getMessage());
        }
        
        // Liberar memoria
        matrizImagen.release();
        imagenGris.release();
        imagenDesenfoque.release();
        imagenBinarizada.release();
        
        return imagenProcesada != null ? imagenProcesada : imagenOriginal;
    }

    // Sobrecarga para mantener compatibilidad si se llama sin carpeta (guarda en /resultados por defecto)
    public File limpiarImagen(File imagenOriginal) {
        return limpiarImagen(imagenOriginal, "resultados");
    }
}
