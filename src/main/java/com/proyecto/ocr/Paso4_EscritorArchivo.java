// Paquete donde se encuentra la clase
package com.proyecto.ocr;

// Importaciones necesarias para escribir texto en archivos de forma eficiente
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// Definición de la clase encargada de escribir en un archivo de texto plano
public class Paso4_EscritorArchivo {

    // Método que recibe la ruta de destino, el número de página, el contenido de texto y un booleano para saber si es la primera página
    public void escribirResultado(String rutaArchivo, int numeroPagina, String contenido, boolean esPrimeraPagina) {
        // Crea una referencia de archivo apuntando a la ruta donde queremos guardar el texto
        File archivo = new File(rutaArchivo);
        
        // Verifica si la carpeta padre (ej. "resultados") no existe, y si es así, la crea automáticamente
        File carpetaPadre = archivo.getParentFile();
        if (carpetaPadre != null && !carpetaPadre.exists()) {
            carpetaPadre.mkdirs();
        }
        
        // Bloque try-with-resources que abre un BufferedWriter envuelto en un FileWriter. 
        // El parámetro '!esPrimeraPagina' indica 'append'. Si no es la primera página, será true (agrega texto al final sin borrar). Si es la primera, será false (sobrescribe todo).
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo, !esPrimeraPagina))) {
            // Escribe en el archivo un encabezado decorativo con el número de la página actual
            escritor.write("--- Página " + numeroPagina + " ---");
            // Inserta un salto de línea compatible con el sistema operativo
            escritor.newLine(); 
            // Escribe todo el contenido de texto que extrajo el OCR
            escritor.write(contenido);
            // Inserta un salto de línea después del contenido
            escritor.newLine();
            // Inserta un segundo salto de línea para dejar espacio vacío antes de la siguiente página
            escritor.newLine(); 
        } catch (IOException e) {
            // Si ocurre algún problema de permisos o escritura, se atrapa la excepción y se imprime el error en consola
            System.err.println("Error al escribir en el archivo de resultados: " + e.getMessage());
        } // Fin del try-with-resources, el archivo se cierra y se guarda automáticamente al llegar aquí
    }
}
