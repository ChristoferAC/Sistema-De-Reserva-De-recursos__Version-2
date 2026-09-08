package sistema.reservas.Data.PDF;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.awt.Desktop;
import java.io.File;
import java.util.List;

/**
 * Utilidad compartida para generar reportes en PDF (requisito del
 * enunciado: todas las funcionalidades deben poder generar un reporte).
 *
 * No tiene Vista ni Controller propios: cualquier Controller que ya
 * exista (ActividadController, CalendarizacionController, etc.) llama
 * a generar(...) pasándole el título y los datos de su tabla, y esta
 * clase arma el PDF con iText. Vive en Data.pdf porque, igual que
 * Data.llm, es una integración con una librería externa, no lógica de
 * negocio de un módulo en particular.
 */
public class GeneradorPDF {

    /**
     * Genera un PDF con una tabla simple (encabezados + filas de texto)
     * y lo abre automáticamente al terminar.
     *
     * @param rutaDestino ruta del archivo a crear, ej. "actividades.pdf"
     * @param titulo      título que aparece arriba del reporte
     * @param columnas    encabezados de la tabla
     * @param filas       cada elemento es una fila; debe tener el mismo
     *                    tamaño que columnas
     */
    public void generar(String rutaDestino, String titulo, String[] columnas, List<String[]> filas) throws Exception {
        PdfFont fuente = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        PdfWriter writer = new PdfWriter(rutaDestino);
        PdfDocument pdfDocument = new PdfDocument(writer);

        // Decisión de diseño: horizontal (landscape), porque Calendarización
        // y Actividades suelen tener varias columnas (un recurso o un día
        // por columna) y no entran bien en una hoja vertical normal.
        Document documento = new Document(pdfDocument, PageSize.A4.rotate());
        documento.setMargins(20, 20, 20, 20);

        Paragraph tituloParrafo = new Paragraph(titulo)
                .setFont(fuente)
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER);
        documento.add(tituloParrafo);

        Table tabla = new Table(columnas.length);
        tabla.setWidth(UnitValue.createPercentValue(100));

        for (String columna : columnas) {
            Paragraph texto = new Paragraph(columna).setFont(fuente).setBold();
            tabla.addCell(getCell(texto, TextAlignment.CENTER));
        }

        for (String[] fila : filas) {
            for (String valor : fila) {
                Paragraph texto = new Paragraph(valor == null ? "" : valor).setFont(fuente);
                tabla.addCell(getCell(texto, TextAlignment.LEFT));
            }
        }

        documento.add(tabla);
        documento.close();

        abrirPdf(rutaDestino);
    }

    private Cell getCell(Paragraph contenido, TextAlignment alineacion) {
        Cell cell = new Cell().add(contenido);
        cell.setPadding(4);
        cell.setTextAlignment(alineacion);
        cell.setBorder(Border.NO_BORDER);
        cell.setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(0.5f));
        return cell;
    }

    private void abrirPdf(String path) {
        try {
            File pdfFile = new File(path);
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    System.out.println("Esta plataforma no soporta abrir archivos automáticamente.");
                }
            } else {
                System.out.println("El archivo PDF no se generó.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}