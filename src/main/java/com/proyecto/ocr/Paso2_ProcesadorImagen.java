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

    // Método que recibe un archivo de imagen original y devuelve un archivo de imagen procesado
    public File limpiarImagen(File imagenOriginal) {
        // Lee la imagen original desde su ruta absoluta y la convierte en una matriz (Mat) de OpenCV
        Mat matrizImagen = opencv_imgcodecs.imread(imagenOriginal.getAbsolutePath());
        
        // Crea una matriz vacía para almacenar la versión en escala de grises de la imagen
        Mat imagenGris = new Mat();
        // Crea una matriz vacía para almacenar la versión desenfocada (sin ruido) de la imagen
        Mat imagenDesenfoque = new Mat();
        // Crea una matriz vacía para almacenar la imagen final en blanco y negro (binarizada)
        Mat imagenBinarizada = new Mat();
        
        // Aplica un filtro para convertir la imagen original (matrizImagen) a escala de grises y lo guarda en imagenGris
        opencv_imgproc.cvtColor(matrizImagen, imagenGris, opencv_imgproc.COLOR_BGR2GRAY);
        // Aplica un desenfoque gaussiano a la imagen en grises para reducir el ruido, usando un núcleo de 3x3
        opencv_imgproc.GaussianBlur(imagenGris, imagenDesenfoque, new Size(3, 3), 0);
        // Aplica binarización de Otsu para convertir la imagen desenfocada a blanco y negro puro, guardándola en imagenBinarizada
        opencv_imgproc.threshold(imagenDesenfoque, imagenBinarizada, 0, 255, opencv_imgproc.THRESH_BINARY | opencv_imgproc.THRESH_OTSU);
        
        // Declara la variable para el archivo de la imagen procesada e inicializa en null
        File imagenProcesada = null;
        // Inicia un bloque try-catch para manejar posibles errores al crear el archivo
        try {
            // Extraemos el nombre original, por ejemplo: "1_imagen_pdf_documento.png"
            String nombreOriginal = imagenOriginal.getName();
            
            // Separamos las partes del nombre para construir uno nuevo más limpio
            // baseDocumento será "documento.png"
            String baseDocumento = nombreOriginal.split("imagen_pdf_")[1];
            // numeroPagina será "1"
            String numeroPagina = nombreOriginal.split("_")[0];
            
            // Construimos el nombre exacto que pediste ("2_imagen_limpia") y le añadimos
            // el nombre del documento y la página al final para que no se sobreescriban si hay muchas páginas
            String nombreImagenLimpia = "resultados/2_imagen_limpia_" + baseDocumento.replace(".png", "") + "_pag" + numeroPagina + ".png";
            
            imagenProcesada = new File(nombreImagenLimpia);
            // Escribe la matriz binarizada dentro del nuevo archivo temporal usando la ruta absoluta
            opencv_imgcodecs.imwrite(imagenProcesada.getAbsolutePath(), imagenBinarizada);
        } catch (Exception e) {
            // Si ocurre un error, imprime el mensaje del error en la consola
            System.err.println("Error al procesar y guardar la imagen: " + e.getMessage());
        }
        
        // Libera la memoria RAM ocupada por la matriz de la imagen original
        matrizImagen.release();
        // Libera la memoria de la matriz en escala de grises
        imagenGris.release();
        // Libera la memoria de la matriz desenfocada
        imagenDesenfoque.release();
        // Libera la memoria de la matriz binarizada
        imagenBinarizada.release();
        
        // Si la imagen procesada se creó con éxito, la devuelve; si no, devuelve la imagen original como respaldo
        return imagenProcesada != null ? imagenProcesada : imagenOriginal;
    }
}
