package sistema.reservas.Presentation.Calendarizacion;

import sistema.reservas.Data.PDF.GeneradorPDF;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Presentation.Calendarizacion.Services.ServiceCalendario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Controller de Calendarización. Responsable: Integrante 3.
 *
 * Puebla el combo de categorías, arma la matriz al presionar "Cargar"
 * y genera el PDF al presionar "Imprimir".
 *
 * Recibe un Supplier<List<CategoriaRecurso>> (en vez de depender
 * directo de CategoriaRecursoService) para no acoplarse a un módulo
 * de otro integrante — igual decisión que se tomó desde el principio.
 */
public class ControllerCalendario {

    private final ViewCalendario view;
    private final ServiceCalendario service;
    private final List<CategoriaRecurso> categorias;

    public ControllerCalendario(ViewCalendario view, ServiceCalendario service, Supplier<List<CategoriaRecurso>> proveedorCategorias) {
        this.view = view;
        this.service = service;
        this.categorias = proveedorCategorias.get();

        poblarComboCategorias();

        view.getBtnCargar().addActionListener(e -> onCargar());
        view.getBtnImprimir().addActionListener(e -> onImprimir());
    }

    private void poblarComboCategorias() {
        JComboBox combo = view.getComboCategoria();
        combo.removeAllItems();
        for (CategoriaRecurso categoria : categorias) {
            combo.addItem(categoria.getNombre());
        }
    }

    private void onCargar() {
        try {
            LocalDate fecha = LocalDate.parse(view.getTxtFecha().getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            CategoriaRecurso categoria = categoriaSeleccionada();

            if (categoria == null) {
                JOptionPane.showMessageDialog(view.getPanel1(), "Elegí una categoría.");
                return;
            }

            ModelCalendario matriz = service.generarMatriz(fecha, categoria);
            pintarMatriz(matriz);

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), "Fecha inválida. Use el formato AAAA-MM-DD.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private CategoriaRecurso categoriaSeleccionada() {
        int indice = view.getComboCategoria().getSelectedIndex();
        if (indice < 0 || indice >= categorias.size()) {
            return null;
        }
        return categorias.get(indice);
    }

    private void pintarMatriz(ModelCalendario matriz) {
        DefaultTableModel modelo = view.getTableModel();
        modelo.setRowCount(0);
        modelo.setColumnCount(0);

        modelo.addColumn("Hora");
        List<Recurso> recursos = matriz.getRecursos();
        for (Recurso recurso : recursos) {
            modelo.addColumn(recurso.getNombre());
        }

        List<LocalTime> horas = matriz.getHoras();
        for (int fila = 0; fila < horas.size(); fila++) {
            Object[] filaDatos = new Object[recursos.size() + 1];
            filaDatos[0] = horas.get(fila).toString();
            for (int columna = 0; columna < recursos.size(); columna++) {
                filaDatos[columna + 1] = matriz.getCelda(fila, columna);
            }
            modelo.addRow(filaDatos);
        }
    }

    private void onImprimir() {
        DefaultTableModel modelo = view.getTableModel();

        if (modelo.getColumnCount() == 0) {
            JOptionPane.showMessageDialog(view.getPanel1(), "Primero cargá una fecha y categoría antes de imprimir.");
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
            new GeneradorPDF().generar("calendarizacion.pdf", "Calendarización de recursos", columnas, filas);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), "No se pudo generar el PDF: " + ex.getMessage());
        }
    }
}