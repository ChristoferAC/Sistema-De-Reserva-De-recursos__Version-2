package sistema.reservas.Presentation.Recurso;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Presentation.Recurso.Services.RecursoService;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class RecursoController {

    private final RecursoPanel view;
    private final RecursoModel model;
    private final RecursoService recursoService;

    public RecursoController(RecursoPanel view, RecursoModel model, RecursoService recursoService) {
        this.view = view;
        this.model = model;
        this.recursoService = recursoService;

        view.setModel(model);

        view.getBtnNuevo().addActionListener(e -> limpiar());
        view.getBtnCancelar().addActionListener(e -> limpiar());
        view.getBtnGuardar().addActionListener(e -> guardar());
        view.getBtnEditar().addActionListener(e -> editar());
        view.getBtnEliminar().addActionListener(e -> eliminar());
        view.getBtnBuscar().addActionListener(e -> buscar());
        view.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        model.setCategorias(recursoService.listarCategorias());
        model.setRecursos(recursoService.listarRecursos());
    }

    private void guardar() {
        model.setCategorias(recursoService.listarCategorias()); // por si se creó una categoría nueva en esta sesión
        if (!validate()) {
            JOptionPane.showMessageDialog(view.getPanel(),
                    "Revise los campos marcados (pase el mouse sobre ellos para ver el detalle).",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Recurso recurso = take();
        try {
            recursoService.crearRecurso(recurso);
            JOptionPane.showMessageDialog(view.getPanel(), "RECURSO REGISTRADO", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            model.setRecursos(recursoService.listarRecursos());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editar() {
        model.setCategorias(recursoService.listarCategorias());
        if (!validate()) {
            JOptionPane.showMessageDialog(view.getPanel(),
                    "Revise los campos marcados (pase el mouse sobre ellos para ver el detalle).",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Recurso recurso = take();
        try {
            recursoService.modificarRecurso(recurso);
            JOptionPane.showMessageDialog(view.getPanel(), "RECURSO MODIFICADO", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            model.setRecursos(recursoService.listarRecursos());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        int fila = view.getTabla().getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(view.getPanel(), "Seleccione un recurso.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(view.getPanel(),
                "¿Desea eliminar el recurso seleccionado?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String id = view.getTableModel().getValueAt(fila, 0).toString();
            recursoService.eliminarRecurso(id);
            JOptionPane.showMessageDialog(view.getPanel(), "RECURSO ELIMINADO", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            model.setRecursos(recursoService.listarRecursos());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscar() {
        String filtro = view.getTxtFiltroCategoria().getText() == null
                ? "" : view.getTxtFiltroCategoria().getText().trim().toLowerCase();

        List<Recurso> todos = recursoService.listarRecursos();
        if (filtro.isEmpty()) {
            model.setRecursos(todos);
            return;
        }

        List<Recurso> filtrados = new ArrayList<>();
        for (Recurso recurso : todos) {
            String categoria = recurso.getCategoria() != null ? recurso.getCategoria().getDescripcion() : "";
            if (categoria.toLowerCase().contains(filtro)) {
                filtrados.add(recurso);
            }
        }
        model.setRecursos(filtrados);
    }

    private void limpiar() {
        model.setCurrent(null);
    }

    private void cargarSeleccion() {
        int fila = view.getTabla().getSelectedRow();
        if (fila < 0) return;
        String id = view.getTableModel().getValueAt(fila, 0).toString();
        model.setCurrent(recursoService.buscarRecurso(id));
    }

    private Recurso take() {
        return new Recurso(
                view.getTxtId().getText().trim(),
                view.getTxtNombre().getText().trim(),
                view.getTxtDescripcion().getText().trim(),
                obtenerCategoriaSeleccionada());
    }

    private CategoriaRecurso obtenerCategoriaSeleccionada() {
        int indice = view.getCmbCategoria().getSelectedIndex();
        List<CategoriaRecurso> categorias = model.getCategorias();
        if (indice < 0 || indice >= categorias.size()) {
            return null;
        }
        return categorias.get(indice);
    }

    private boolean validate() {
        boolean valido = true;

        if (view.getTxtId().getText().trim().isEmpty()) {
            valido = false;
            view.getTxtId().setToolTipText("ID requerido");
        } else {
            view.getTxtId().setToolTipText(null);
        }

        if (view.getTxtNombre().getText().trim().isEmpty()) {
            valido = false;
            view.getTxtNombre().setToolTipText("Nombre requerido");
        } else {
            view.getTxtNombre().setToolTipText(null);
        }

        if (view.getTxtDescripcion().getText().trim().isEmpty()) {
            valido = false;
            view.getTxtDescripcion().setToolTipText("Descripción requerida");
        } else {
            view.getTxtDescripcion().setToolTipText(null);
        }

        if (obtenerCategoriaSeleccionada() == null) {
            valido = false;
            view.getCmbCategoria().setToolTipText("Categoría requerida");
        } else {
            view.getCmbCategoria().setToolTipText(null);
        }

        return valido;
    }
}