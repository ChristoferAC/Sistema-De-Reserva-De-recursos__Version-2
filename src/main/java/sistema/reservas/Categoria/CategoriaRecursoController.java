package sistema.reservas.Categoria;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class CategoriaRecursoController {

    private final CategoriaPanel view;
    private final CategoriaRecursoService categoriaService;

    public CategoriaRecursoController(CategoriaPanel view, CategoriaRecursoService categoriaService) {
        this.view = view;
        this.categoriaService = categoriaService;

        this.view.getBtnBuscar().addActionListener(e -> buscar());
        this.view.getBtnGuardar().addActionListener(e -> guardar());
        this.view.getBtnBorrar().addActionListener(e -> borrar());
        this.view.getBtnLimpiar().addActionListener(e -> view.limpiarFormulario());
        this.view.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        cargarTabla(categoriaService.listarTodas());
    }

    private void buscar() {
        String descripcion = view.getTxtBuscarDescripcion().getText().trim();
        if (descripcion.isEmpty()) {
            cargarTabla(categoriaService.listarTodas());
        } else {
            cargarTabla(categoriaService.buscarPorDescripcion(descripcion));
        }
    }

    private void guardar() {
        try {
            String idTexto = view.getTxtId().getText().trim();
            String descripcion = view.getTxtDescripcion().getText().trim();

            if (idTexto.isEmpty()) {
                CategoriaRecurso nueva = new CategoriaRecurso(0, "", descripcion);
                // id 0: lo asigna el DAO al guardar (autogenerado)
                categoriaService.crear(nueva);
            } else {
                int id = Integer.parseInt(idTexto);
                CategoriaRecurso existente = categoriaService.buscarPorId(id);
                if (existente == null) {
                    JOptionPane.showMessageDialog(view, "No existe esa categoría.");
                    return;
                }
                existente.setDescripcion(descripcion);
                categoriaService.actualizar(existente);
            }

            view.limpiarFormulario();
            cargarTabla(categoriaService.listarTodas());

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "El ID debe ser numérico.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage());
        }
    }

    private void borrar() {
        String idTexto = view.getTxtId().getText().trim();
        if (idTexto.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Seleccione una categoría para borrar.");
            return;
        }
        try {
            int id = Integer.parseInt(idTexto);
            categoriaService.eliminar(id);
            view.limpiarFormulario();
            cargarTabla(categoriaService.listarTodas());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "El ID debe ser numérico.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage());
        }
    }

    private void cargarSeleccion() {
        int fila = view.getTabla().getSelectedRow();
        if (fila < 0) return;

        DefaultTableModel model = view.getTableModel();
        int id = Integer.parseInt(model.getValueAt(fila, 0).toString());
        String descripcion = model.getValueAt(fila, 1).toString();

        view.cargarFormulario(id, descripcion);
    }

    private void cargarTabla(List<CategoriaRecurso> categorias) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (CategoriaRecurso c : categorias) {
            model.addRow(new Object[]{c.getId(), c.getDescripcion()});
        }
    }
}