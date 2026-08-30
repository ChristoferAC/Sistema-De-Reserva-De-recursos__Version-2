package sistema.reservas.controller;

import sistema.reservas.model.CategoriaRecurso;
import sistema.reservas.model.Recurso;
import sistema.reservas.service.CalendarizacionService;
import sistema.reservas.service.MatrizCalendarizacion;
import sistema.reservas.view.CalendarizacionPanel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Supplier;

public class CalendarizacionController {

    private final CalendarizacionPanel panel;
    private final CalendarizacionService service;
    private final Supplier<List<CategoriaRecurso>> proveedorCategorias;

    public CalendarizacionController(CalendarizacionPanel panel,
                                     CalendarizacionService service,
                                     Supplier<List<CategoriaRecurso>> proveedorCategorias) {
        this.panel = panel;
        this.service = service;
        this.proveedorCategorias = proveedorCategorias;

        cargarCategorias();
        panel.getBtnCargar().addActionListener(e -> onCargar());
    }

    private void cargarCategorias() {
        panel.getComboCategoria().removeAllItems();
        for (CategoriaRecurso categoria : proveedorCategorias.get()) {
            panel.getComboCategoria().addItem(categoria);
        }
    }

    private void onCargar() {
        try {
            LocalDate fecha = LocalDate.parse(panel.getTxtFecha().getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            CategoriaRecurso categoria = (CategoriaRecurso) panel.getComboCategoria().getSelectedItem();

            if (categoria == null) {
                JOptionPane.showMessageDialog(panel, "Seleccione una categoría.");
                return;
            }

            MatrizCalendarizacion matriz = service.generarMatriz(fecha, categoria);
            pintarMatriz(matriz);

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(panel, "Fecha inválida. Use el formato AAAA-MM-DD.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(panel, ex.getMessage());
        }
    }

    private void pintarMatriz(MatrizCalendarizacion matriz) {
        DefaultTableModel modelo = panel.getTableModel();
        modelo.setRowCount(0);
        modelo.setColumnCount(0);

        modelo.addColumn("Hora");
        for (Recurso recurso : matriz.getRecursos()) {
            modelo.addColumn(recurso.getNombre());
        }

        List<LocalTime> horas = matriz.getHoras();
        int cantidadRecursos = matriz.getRecursos().size();

        for (int fila = 0; fila < horas.size(); fila++) {
            Object[] filaDatos = new Object[cantidadRecursos + 1];
            filaDatos[0] = horas.get(fila).toString();
            for (int columna = 0; columna < cantidadRecursos; columna++) {
                filaDatos[columna + 1] = matriz.getCelda(fila, columna);
            }
            modelo.addRow(filaDatos);
        }
    }
}
