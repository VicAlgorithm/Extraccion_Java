// Paquete donde se encuentra la clase
package com.proyecto.ocr;

// Importaciones de Tess4J (la envoltura de Java para Tesseract) y excepciones
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

// Importación para el manejo de archivos
import java.io.File;

// Definición de la clase pública para extraer texto mediante OCR
public class Paso3_AnalizadorTexto {

    // Declara una variable privada del tipo ITesseract que será la instancia del motor de OCR
    private ITesseract tesseract;

    // Constructor de la clase que se ejecuta al instanciar (crear un objeto) esta clase
    public Paso3_AnalizadorTexto() {
        // Inicializa la instancia del motor Tesseract
        tesseract = new Tesseract();
        // Establecemos la ruta a la carpeta "diccionarios" que contiene los archivos de idioma
        tesseract.setDatapath(new File("diccionarios").getAbsolutePath());
        // Le indicamos que lea en idioma español ("spa") usando el archivo que descargamos
        tesseract.setLanguage("spa");
    }

    // Método que recibe un archivo de imagen y devuelve el texto reconocido en formato String
    public String extraerTexto(File imagen) {
        // Inicia un bloque try-catch para manejar errores específicos de Tesseract
        try {
            // Ejecuta el motor OCR sobre el archivo de imagen y devuelve el texto extraído
            return tesseract.doOCR(imagen);
        } catch (TesseractException e) {
            // Si Tesseract falla, imprime un mensaje de error en la consola de errores estándar
            System.err.println("Error al procesar el OCR en la imagen: " + e.getMessage());
            // Devuelve un mensaje por defecto indicando que hubo un error
            return "[Error al extraer texto]";
        }
    }
}
