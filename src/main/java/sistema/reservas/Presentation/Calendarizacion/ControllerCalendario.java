package sistema.reservas.Presentation.Calendarizacion;

import sistema.reservas.Data.PDF.GeneradorPDF;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Presentation.Calendarizacion.Services.ServiceCalendario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Controller de Calendarización. Responsable: Integrante 3.
 *
 * Ya NO pinta la tabla directamente: le pide la matriz a ServiceCalendario
 * y se la entrega al Model (model.setMatriz(...)). Es la View, escuchando
 * el Model vía PropertyChangeListener, la que se encarga de pintarse sola.
 */
public class ControllerCalendario {

    private final ViewCalendario view;
    private final ModelCalendario model;
    private final ServiceCalendario service;
    private final List<CategoriaRecurso> categorias;

    public ControllerCalendario(ViewCalendario view, ModelCalendario model, ServiceCalendario service, Supplier<List<CategoriaRecurso>> proveedorCategorias) {
        this.view = view;
        this.model = model;
        this.service = service;
        this.categorias = proveedorCategorias.get();

        view.setModel(model);

        poblarComboCategorias();

        view.getBtnCargar().addActionListener(e -> onCargar());
        view.getBtnImprimir().addActionListener(e -> onImprimir());
    }

    private void poblarComboCategorias() {
        JComboBox combo = view.getComboCategoria();
        combo.removeAllItems();
        for (CategoriaRecurso categoria : categorias) {
            combo.addItem(textoCategoria(categoria));
        }
    }

    private String textoCategoria(CategoriaRecurso categoria) {
        String nombre = categoria.getNombre();
        if (nombre != null && !nombre.isBlank()) {
            return nombre;
        }
        return categoria.getDescripcion();
    }

    private void onCargar() {
        try {
            LocalDate fecha = LocalDate.parse(view.getTxtFecha().getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            CategoriaRecurso categoria = categoriaSeleccionada();

            if (categoria == null) {
                JOptionPane.showMessageDialog(view.getPanel1(), "Elegí una categoría.");
                return;
            }

            MatrizCalendario matriz = service.generarMatriz(fecha, categoria);
            model.setMatriz(matriz); // <-- antes aquí se llamaba pintarMatriz(matriz) directo

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