package sistema.reservas.Presentation.Categoria;

import sistema.reservas.Logic.CategoriaRecurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CategoriaRecursoController {

    private final CategoriaPanel view;
    private final CategoriaModel model;
    private final CategoriaRecursoService categoriaService;

    public CategoriaRecursoController(CategoriaPanel view, CategoriaModel model) {
        this.view = view;
        this.model = model;
        this.categoriaService = new CategoriaRecursoService();

        view.setModel(model);

        this.view.getBtnBuscar().addActionListener(e -> buscar());
        this.view.getBtnGuardar().addActionListener(e -> guardar());
        this.view.getBtnBorrar().addActionListener(e -> borrar());
        this.view.getBtnLimpiar().addActionListener(e -> limpiar());
        this.view.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        model.setCategorias(categoriaService.listarTodas());
    }

    private void buscar() {
        String descripcion = view.getTxtBuscarDescripcion().getText().trim();
        if (descripcion.isEmpty()) {
            model.setCategorias(categoriaService.listarTodas());
        } else {
            model.setCategorias(categoriaService.buscarPorDescripcion(descripcion));
        }
    }

    private void guardar() {
        try {
            String idTexto = view.getTxtId().getText().trim();
            String descripcion = view.getTxtDescripcion().getText().trim();

            if (idTexto.isEmpty()) {
                CategoriaRecurso nueva = new CategoriaRecurso(0, "", descripcion);
                // id 0: lo asigna el Service al guardar (autogenerado)
                categoriaService.crear(nueva);
            } else {
                int id = Integer.parseInt(idTexto);
                CategoriaRecurso existente = categoriaService.buscarPorId(id);
                if (existente == null) {
                    JOptionPane.showMessageDialog(view.getPanel1(), "No existe esa categoría.");
                    return;
                }
                existente.setDescripcion(descripcion);
                categoriaService.actualizar(existente);
            }

            model.setCurrent(null);
            model.setCategorias(categoriaService.listarTodas());

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), "El ID debe ser numérico.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void borrar() {
        String idTexto = view.getTxtId().getText().trim();
        if (idTexto.isEmpty()) {
            JOptionPane.showMessageDialog(view.getPanel1(), "Seleccione una categoría para borrar.");
            return;
        }
        try {
            int id = Integer.parseInt(idTexto);
            categoriaService.eliminar(id);
            model.setCurrent(null);
            model.setCategorias(categoriaService.listarTodas());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), "El ID debe ser numérico.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void limpiar() {
        model.setCurrent(null);
    }

    private void cargarSeleccion() {
        int fila = view.getTabla().getSelectedRow();
        if (fila < 0) return;

        DefaultTableModel tableModel = view.getTableModel();
        int id = Integer.parseInt(tableModel.getValueAt(fila, 0).toString());

        model.setCurrent(categoriaService.buscarPorId(id));
    }
}