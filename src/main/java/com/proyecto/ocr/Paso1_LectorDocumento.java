// Paquete donde se encuentra esta clase, parte de la estructura del proyecto
package com.proyecto.ocr;

// Importaciones de la librería Apache PDFBox para manejar documentos PDF
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

// Importaciones nativas de Java para manejar imágenes y archivos
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Definición de la clase pública que se encarga de leer el PDF
public class Paso1_LectorDocumento {
    
    // Método público que recibe un archivo PDF, puede lanzar una excepción de entrada/salida (IOException), y devuelve una lista de imágenes (archivos)
    public List<File> convertirPdfAImagenes(File archivoPdf) throws IOException {
        // Inicializa una lista dinámica para guardar las referencias a las imágenes generadas
        List<File> imagenesGeneradas = new ArrayList<>();
        
        // Bloque try-with-resources que abre el documento PDF y asegura que se cierre automáticamente al terminar
        try (PDDocument documento = PDDocument.load(archivoPdf)) {
            // Crea un renderizador de PDF que se encargará de "dibujar" las páginas
            PDFRenderer renderizador = new PDFRenderer(documento);
            // Obtiene el número total de páginas que contiene el PDF
            int totalPaginas = documento.getNumberOfPages();
            
            // Bucle for que itera desde la página 0 hasta la última página del documento
            for (int i = 0; i < totalPaginas; i++) {
                // Renderiza la página actual (i) a una imagen original completa
                BufferedImage imagenCompleta = renderizador.renderImageWithDPI(i, 300, ImageType.RGB);
                
                // ==========================================
                // LÓGICA DE RECORTE DE IMAGEN
                // ==========================================
                int anchoOriginal = imagenCompleta.getWidth();
                int altoOriginal = imagenCompleta.getHeight();
                
                // El número 0.55 significa que nos quedaremos solo con el 55% superior de la página
                // (cortando así el mapa u otras cosas que estén en la mitad inferior). 
                // ¡Puedes cambiar este 0.55 por 0.60 o 0.40 hasta que quede exacto donde lo quieres!
                int altoRecortado = (int) (altoOriginal * 0.55); 
                
                // Recortamos la imagen: Empieza en (0,0) arriba a la izquierda, toma todo el ancho, pero solo el alto recortado
                BufferedImage imagen = imagenCompleta.getSubimage(0, 0, anchoOriginal, altoRecortado);
                // ==========================================
                // Obtenemos el nombre original del archivo PDF (ej. "documento.pdf")
                String nombreOriginalDelPdf = archivoPdf.getName();
                
                // Le quitamos el ".pdf" al nombre para tener el nombre limpio (ej. "documento")
                String nombreLimpio = nombreOriginalDelPdf.replace(".pdf", "");
                
                // Construimos la ruta exacta dentro de la carpeta "resultados"
                String rutaFinalParaLaImagen = "resultados/" + (i + 1) + "_imagen_pdf_" + nombreLimpio + ".png";
                
                // Creamos un archivo real en esa ruta
                File archivoImagen = new File(rutaFinalParaLaImagen);
                
                // Nos aseguramos de que la carpeta "resultados" exista, si no, la creamos
                if (archivoImagen.getParentFile() != null && !archivoImagen.getParentFile().exists()) {
                    archivoImagen.getParentFile().mkdirs();
                }
                
                // Escribe los datos de la imagen RECORTADA dentro de ese archivo nuevo
                // (Como 'imagen' ya es la versión recortada con getSubimage, esto es lo que se guardará y se mandará a Tesseract)
                ImageIO.write(imagen, "png", archivoImagen);
                
                // Añade el archivo recién creado a nuestra lista de imágenes generadas
                imagenesGeneradas.add(archivoImagen);
            }
        } // Fin del bloque try, aquí se cierra 'documento' de forma automática
        
        // Devuelve la lista completa de las imágenes temporales creadas
        return imagenesGeneradas;
    }
}
