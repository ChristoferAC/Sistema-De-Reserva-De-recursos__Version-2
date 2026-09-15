package sistema.reservas.Presentation.Actividad;

import sistema.reservas.Data.PDF.GeneradorPDF;
import sistema.reservas.Presentation.Actividad.Services.ServiceActividad;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller de Actividades. Responsable: Integrante 3.
 *
 * Ya NO pinta la tabla directamente: le pide la matriz semanal al
 * ServiceActividad y se la entrega al Model (model.setMatriz(...)).
 * Es la View, escuchando el Model via PropertyChangeListener, la que
 * se encarga de pintarse a sí misma.
 */
public class ControllerActividad {

    private final ViewActividad view;
    private ModelActividad model;
    private final ServiceActividad service;

    public ControllerActividad(ViewActividad view, ModelActividad modelActividad, ServiceActividad service) {
        this.view = view;
        this.model = modelActividad;
        this.service = service;

        view.setModel(model);

        view.getBtnCargar().addActionListener(e -> onCargar());
        view.getBtnImprimir().addActionListener(e -> onImprimir());
    }

    private void onCargar() {
        try {
            LocalDate fecha = LocalDate.parse(
                    view.getTxtFechaReferencia().getText().trim(),
                    DateTimeFormatter.ISO_LOCAL_DATE);

            MatrizActividad matriz = service.generarMatriz(fecha);
            model.setMatriz(matriz); // <-- antes aquí se llamaba pintarMatriz(matriz) directo

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), "Fecha inválida. Use el formato AAAA-MM-DD.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void onImprimir() {
        // Esto no cambia: lee la tabla ya pintada (por la View) y genera el PDF.
        DefaultTableModel modelo = view.getTableModel();

        if (modelo.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(view.getPanel1(), "Primero cargá una semana antes de imprimir.");
            return;
        }

        String[] columnas = new String[modelo.getColumnCount()];
        for (int i = 0; i < columnas.length; i++) {
            columnas[i] = modelo.getColumnName(i);
        }

        List<String[]> filas = new ArrayList<>();
        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
            String[] datosFila = new String[modelo.getColumnCount()];
            for (int columna = 0; columna < modelo.getColumnCount(); columna++) {
                Object valor = modelo.getValueAt(fila, columna);
                datosFila[columna] = valor == null ? "" : valor.toString();
            }
            filas.add(datosFila);
        }

        try {
            new GeneradorPDF().generar("actividades.pdf", "Actividades semanales", columnas, filas);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), "No se pudo generar el PDF: " + ex.getMessage());
        }
    }
}