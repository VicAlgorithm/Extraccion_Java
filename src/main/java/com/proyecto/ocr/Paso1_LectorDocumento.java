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
    
    // Método que recibe el PDF y la carpeta de destino, y devuelve la lista de imágenes recortadas
    public List<File> convertirPdfAImagenes(File archivoPdf, String carpetaDestino) throws IOException {
        List<File> imagenesGeneradas = new ArrayList<>();
        
        try (PDDocument documento = PDDocument.load(archivoPdf)) {
            PDFRenderer renderizador = new PDFRenderer(documento);
            int totalPaginas = documento.getNumberOfPages();
            
            for (int i = 0; i < totalPaginas; i++) {
                BufferedImage imagenCompleta = renderizador.renderImageWithDPI(i, 300, ImageType.RGB);
                
                // Recorte del 55% superior
                int anchoOriginal = imagenCompleta.getWidth();
                int altoOriginal = imagenCompleta.getHeight();
                int altoRecortado = (int) (altoOriginal * 0.55); 
                
                BufferedImage imagen = imagenCompleta.getSubimage(0, 0, anchoOriginal, altoRecortado);
                
                // Construir ruta: carpetaDestino/1_recorte_base_pagX.png
                String rutaFinalParaLaImagen = carpetaDestino + "/1_recorte_base_pag" + (i + 1) + ".png";
                
                File archivoImagen = new File(rutaFinalParaLaImagen);
                if (archivoImagen.getParentFile() != null && !archivoImagen.getParentFile().exists()) {
                    archivoImagen.getParentFile().mkdirs();
                }
                
                ImageIO.write(imagen, "png", archivoImagen);
                imagenesGeneradas.add(archivoImagen);
            }
        }
        return imagenesGeneradas;
    }

    // Sobrecarga para mantener compatibilidad
    public List<File> convertirPdfAImagenes(File archivoPdf) throws IOException {
        return convertirPdfAImagenes(archivoPdf, "resultados");
    }
}
