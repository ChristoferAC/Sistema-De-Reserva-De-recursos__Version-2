package sistema.reservas.Presentation.Categoria;

import sistema.reservas.Data.PDF.GeneradorPDF;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Services.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class CategoriaRecursoController {

    private final CategoriaPanel view;
    private final CategoriaModel model;

    public CategoriaRecursoController(CategoriaPanel view, CategoriaModel model) {
        this.view = view;
        this.model = model;

        view.setModel(model);

        this.view.getBtnBuscar().addActionListener(e -> buscar());
        this.view.getBtnGuardar().addActionListener(e -> guardar());
        this.view.getBtnBorrar().addActionListener(e -> borrar());
        this.view.getBtnLimpiar().addActionListener(e -> limpiar());
        this.view.getBtnImprimir().addActionListener(e -> imprimir());
        this.view.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        cargarTodas();
    }

    private void cargarTodas() {
        try {
            model.setCategorias(Service.instance().findAll(new CategoriaRecurso()));
        } catch (Exception ex) {
            mostrar(ex.getMessage());
        }
    }

    private void buscar() {
        String descripcion = view.getTxtBuscarDescripcion().getText().trim();
        if (descripcion.isEmpty()) {
            cargarTodas();
            return;
        }
        try {
            model.setCategorias(Service.instance().findCategoriasByDescripcion(descripcion));
        } catch (Exception ex) {
            mostrar(ex.getMessage());
        }
    }

    private void guardar() {
        try {
            String idTexto = view.getTxtId().getText().trim();
            String descripcion = view.getTxtDescripcion().getText().trim();

            if (idTexto.isEmpty()) {
                // id 0: lo genera MySQL al guardar (AUTO_INCREMENT)
                CategoriaRecurso nueva = new CategoriaRecurso(0, "", descripcion);
                Service.instance().create(nueva);
            } else {
                CategoriaRecurso actual = model.getCurrent();
                if (actual == null) {
                    mostrar("No existe esa categoría.");
                    return;
                }
                // Se modifica la copia local y se envia al Backend para que haga el UPDATE
                actual.setDescripcion(descripcion);
                Service.instance().update(actual);
            }

            model.setCurrent(null);
            cargarTodas();

        } catch (Exception ex) {
            mostrar(ex.getMessage());
        }
    }

    private void borrar() {
        String idTexto = view.getTxtId().getText().trim();
        if (idTexto.isEmpty()) {
            mostrar("Seleccione una categoría para borrar.");
            return;
        }
        try {
            CategoriaRecurso categoria = new CategoriaRecurso();
            categoria.setId(Integer.parseInt(idTexto));
            Service.instance().delete(categoria);
            model.setCurrent(null);
            cargarTodas();
        } catch (NumberFormatException ex) {
            mostrar("El ID debe ser numérico.");
        } catch (Exception ex) {
            mostrar(ex.getMessage());
        }
    }

    private void imprimir() {
        DefaultTableModel tableModel = view.getTableModel();
        if (tableModel.getRowCount() == 0) {
            mostrar("No hay categorías para imprimir.");
            return;
        }

        String[] columnas = {"ID", "Descripcion"};
        List<String[]> filas = new ArrayList<>();

        for (int fila = 0; fila < tableModel.getRowCount(); fila++) {
            String[] datosFila = new String[columnas.length];
            for (int columna = 0; columna < columnas.length; columna++) {
                Object valor = tableModel.getValueAt(fila, columna);
                datosFila[columna] = valor == null ? "" : valor.toString();
            }
            filas.add(datosFila);
        }

        try {
            new GeneradorPDF().generar("categorias.pdf", "Listado de Categorías", columnas, filas);
        } catch (Exception ex) {
            mostrar("No se pudo generar el PDF: " + ex.getMessage());
        }
    }

    private void limpiar() {
        model.setCurrent(null);
    }

    private void cargarSeleccion() {
        int fila = view.getTabla().getSelectedRow();
        if (fila < 0) return;

        DefaultTableModel tableModel = view.getTableModel();
        CategoriaRecurso filtro = new CategoriaRecurso();
        filtro.setId(Integer.parseInt(tableModel.getValueAt(fila, 0).toString()));

        try {
            model.setCurrent(Service.instance().read(filtro));
        } catch (Exception ex) {
            mostrar(ex.getMessage());
        }
    }

    private void mostrar(String mensaje) {
        JOptionPane.showMessageDialog(view.getPanel1(), mensaje);
    }
}