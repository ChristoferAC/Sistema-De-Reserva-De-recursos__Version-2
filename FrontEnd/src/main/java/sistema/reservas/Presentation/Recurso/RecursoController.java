// ============================================================
// FRONTEND / src/main/java/sistema/reservas/Presentation/Recurso/RecursoController.java
// Usa Service.instance(). Las categorías vienen del findAll(CategoriaRecurso) de I1:
// ese método debe existir en IService (firma acordada en la guía:
// List<CategoriaRecurso> findAll(CategoriaRecurso)). Mientras I1 no lo publique,
// cargarCategorias() es la única línea que no compila.
// ============================================================
package sistema.reservas.Presentation.Recurso;

import sistema.reservas.Data.PDF.GeneradorPDF;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.IService;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class RecursoController {

    private final RecursoPanel view;
    private final RecursoModel model;
    private final IService service;
    private final Callable<List<CategoriaRecurso>> proveedorCategorias;

    public RecursoController(RecursoPanel view, RecursoModel model,
                             Callable<List<CategoriaRecurso>> proveedorCategorias) {
        this(view, model, Service.instance(), proveedorCategorias);
    }

    /** Para pruebas: permite inyectar un IService falso sin abrir un socket. */
    public RecursoController(RecursoPanel view, RecursoModel model, IService service,
                             Callable<List<CategoriaRecurso>> proveedorCategorias) {
        if (proveedorCategorias == null) {
            throw new IllegalArgumentException("El proveedor de categorías no puede ser nulo.");
        }
        this.view = view;
        this.model = model;
        this.service = service;
        this.proveedorCategorias = proveedorCategorias;;

        view.setModel(model);

        view.getBtnNuevo().addActionListener(e -> limpiar());
        view.getBtnCancelar().addActionListener(e -> limpiar());
        view.getBtnGuardar().addActionListener(e -> guardar());
        view.getBtnEditar().addActionListener(e -> editar());
        view.getBtnEliminar().addActionListener(e -> eliminar());
        view.getBtnBuscar().addActionListener(e -> buscar());
        view.getImprimeButton().addActionListener(e -> imprimir());
        view.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        try {
            refrescarCategorias();
            refrescarRecursos();
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    // ---------------------- datos desde el Service ----------------------

    private void refrescarCategorias() throws Exception {
        model.setCategorias(proveedorCategorias.call());   // por si I1 creó una categoría nueva en esta sesión
    }

    private void refrescarRecursos() throws Exception {
        model.setRecursos(service.findAll(new Recurso()));
    }

    private void mostrarError(Exception ex) {
        String mensaje = ex.getMessage() != null ? ex.getMessage() : ex.toString();
        JOptionPane.showMessageDialog(view.getPanel(), mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------ acciones ------------------------------

    private void guardar() {
        try {
            refrescarCategorias();
        } catch (Exception ex) {
            mostrarError(ex);
            return;
        }
        if (!validate()) {
            avisarDatosInvalidos();
            return;
        }
        Recurso recurso = take();
        try {
            service.create(recurso);
            JOptionPane.showMessageDialog(view.getPanel(), "RECURSO REGISTRADO", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            refrescarRecursos();
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void editar() {
        try {
            refrescarCategorias();
        } catch (Exception ex) {
            mostrarError(ex);
            return;
        }
        if (!validate()) {
            avisarDatosInvalidos();
            return;
        }
        Recurso recurso = take();
        try {
            service.update(recurso);
            JOptionPane.showMessageDialog(view.getPanel(), "RECURSO MODIFICADO", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            refrescarRecursos();
        } catch (Exception ex) {
            mostrarError(ex);
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
            Recurso aEliminar = new Recurso();
            aEliminar.setId(view.getTableModel().getValueAt(fila, 0).toString());
            service.delete(aEliminar);   // si tiene reservas, el mensaje del Backend lo explica
            JOptionPane.showMessageDialog(view.getPanel(), "RECURSO ELIMINADO", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            refrescarRecursos();
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void buscar() {
        String filtro = view.getTxtFiltroCategoria().getText() == null
                ? "" : view.getTxtFiltroCategoria().getText().trim().toLowerCase();

        List<Recurso> todos;
        try {
            todos = service.findAll(new Recurso());
        } catch (Exception ex) {
            mostrarError(ex);
            return;
        }
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

    private void imprimir() {
        DefaultTableModel tableModel = view.getTableModel();
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view.getPanel(),
                    "No hay recursos para imprimir.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] columnas = {"ID", "Nombre", "Descripción", "Categoría"};
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
            new GeneradorPDF().generar("recursos.pdf", "Listado de Recursos", columnas, filas);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel(),
                    "No se pudo generar el PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Sin ir a la red: la fila seleccionada sale de la lista que ya tiene el Model (ids de texto: equals). */
    private void cargarSeleccion() {
        int fila = view.getTabla().getSelectedRow();
        if (fila < 0) return;
        String id = view.getTableModel().getValueAt(fila, 0).toString();
        for (Recurso recurso : model.getRecursos()) {
            if (id.equals(recurso.getId())) {
                model.setCurrent(recurso);
                return;
            }
        }
    }

    // --------------------------- formulario ---------------------------

    private void avisarDatosInvalidos() {
        JOptionPane.showMessageDialog(view.getPanel(),
                "Revise los campos marcados (pase el mouse sobre ellos para ver el detalle).",
                "Datos inválidos", JOptionPane.WARNING_MESSAGE);
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