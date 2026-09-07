package sistema.reservas.Presentation.Actividad;

import sistema.reservas.Data.PDF.GeneradorPDF;
import sistema.reservas.Presentation.Actividad.Services.ServiceActividad;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller de Actividades. Responsable: Integrante 3.
 *
 * Toma la fecha de referencia que el usuario escribió en ActividadPanel,
 * le pide la matriz semanal a ActividadService y pinta el resultado en
 * la tabla. No contiene lógica de negocio (eso vive en el Service).
 */
public class ControllerActividad {

    // Decisión de diseño: nombres de día fijos en español, para no
    // depender de que el entorno donde corra el programa tenga
    // instalados los datos de idioma "es" (evita nombres en inglés
    // si el Locale por defecto de la máquina es otro).
    private static final String[] NOMBRES_DIA = {
            "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
    };

    private final ViewActividad panel;
    private final ServiceActividad service;

    public  ControllerActividad(ViewActividad panel, ServiceActividad service) {
        this.panel = panel;
        this.service = service;

        panel.getBtnCargar().addActionListener(e -> onCargar());
        panel.getBtnImprimir().addActionListener(e -> onImprimir());
    }

    private void onImprimir() {
        DefaultTableModel modelo = panel.getTableModel();

        if (modelo.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(panel.getPanel1(), "Primero cargá una semana antes de imprimir.");
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
            JOptionPane.showMessageDialog(panel.getPanel1(), "No se pudo generar el PDF: " + ex.getMessage());
        }
    }

    private void onCargar() {
        try {
            LocalDate fecha = LocalDate.parse(
                    panel.getTxtFechaReferencia().getText().trim(),
                    DateTimeFormatter.ISO_LOCAL_DATE);

            ModelActividad matriz = service.generarMatriz(fecha);
            pintarMatriz(matriz);

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(panel.getPanel1(), "Fecha inválida. Use el formato AAAA-MM-DD.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(panel.getPanel1(), ex.getMessage());
        }
    }

    private void pintarMatriz(ModelActividad matriz) {
        DefaultTableModel modelo = panel.getTableModel();
        modelo.setRowCount(0);
        modelo.setColumnCount(0);

        modelo.addColumn("Hora");

        List<LocalDate> dias = matriz.getDias();
        for (LocalDate dia : dias) {
            String nombreDia = NOMBRES_DIA[dia.getDayOfWeek().getValue() - 1];
            modelo.addColumn(nombreDia + " " + dia);
        }

        List<LocalTime> horas = matriz.getHoras();
        int cantidadDias = dias.size();

        for (int fila = 0; fila < horas.size(); fila++) {
            Object[] filaDatos = new Object[cantidadDias + 1];
            filaDatos[0] = horas.get(fila).toString();
            for (int columna = 0; columna < cantidadDias; columna++) {
                filaDatos[columna + 1] = matriz.getCelda(fila, columna);
            }
            modelo.addRow(filaDatos);
        }
    }
}