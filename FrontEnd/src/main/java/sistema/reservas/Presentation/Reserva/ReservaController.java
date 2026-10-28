// ============================================================
// FRONTEND / src/main/java/sistema/reservas/Presentation/Reserva/ReservaController.java
// Usa Service.instance(). El id de una reserva nueva lo genera MySQL: se ignora al crear
// y solo se exige al editar.
// ============================================================
package sistema.reservas.Presentation.Reserva;

import sistema.reservas.Data.PDF.GeneradorPDF;
import sistema.reservas.Logic.IService;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Logic.ReservaExtraccion;
import sistema.reservas.Logic.Services.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaController {

    private final ReservaPanel view;
    private final ReservaModel model;
    private final IService service;

    public ReservaController(ReservaPanel view, ReservaModel model) {
        this(view, model, Service.instance());
    }

    /** Para pruebas: permite inyectar un IService falso sin abrir un socket. */
    public ReservaController(ReservaPanel view, ReservaModel model, IService service) {
        this.view = view;
        this.model = model;
        this.service = service;

        view.setModel(model);

        view.getBtnNueva().addActionListener(e -> limpiar());
        view.getBtnLimpiar().addActionListener(e -> limpiar());
        view.getBtnReservar().addActionListener(e -> reservar());
        view.getBtnEditar().addActionListener(e -> editar());
        view.getBtnCancelar().addActionListener(e -> cancelar());
        view.getBtnUsarIA().addActionListener(e -> usarIA());
        view.getImprimeButton().addActionListener(e -> imprimir());
        view.getTablaReservas().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        try {
            refrescarRecursos();
            refrescarReservas();
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    // ---------------------- datos desde el Service ----------------------

    private void refrescarRecursos() throws Exception {
        model.setRecursos(service.findAll(new Recurso()));   // por si se creó un recurso nuevo en esta sesión
    }

    private void refrescarReservas() throws Exception {
        model.setReservas(service.findAll(new Reserva()));
    }

    private void mostrarError(Exception ex) {
        String mensaje = ex.getMessage() != null ? ex.getMessage() : ex.toString();
        JOptionPane.showMessageDialog(view.getPanel(), mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------ acciones ------------------------------

    private void reservar() {
        try {
            refrescarRecursos();
        } catch (Exception ex) {
            mostrarError(ex);
            return;
        }
        if (!validate(false)) {
            avisarDatosInvalidos();
            return;
        }
        Reserva reserva = take(false);
        try {
            service.create(reserva);   // devuelve la reserva con el id generado por MySQL
            JOptionPane.showMessageDialog(view.getPanel(), "RESERVA APLICADA", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            refrescarReservas();
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void editar() {
        try {
            refrescarRecursos();
        } catch (Exception ex) {
            mostrarError(ex);
            return;
        }
        if (!validate(true)) {
            avisarDatosInvalidos();
            return;
        }
        Reserva reserva = take(true);
        try {
            service.update(reserva);
            JOptionPane.showMessageDialog(view.getPanel(), "RESERVA MODIFICADA", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            refrescarReservas();
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void cancelar() {
        int fila = view.getTablaReservas().getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(view.getPanel(), "Seleccione una reserva.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(view.getPanel(),
                "¿Desea cancelar la reserva seleccionada?",
                "Confirmar cancelación", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            int id = Integer.parseInt(view.getTableModel().getValueAt(fila, 0).toString());
            Reserva aCancelar = new Reserva();
            aCancelar.setId(id);
            service.cancelar(aCancelar);
            JOptionPane.showMessageDialog(view.getPanel(), "RESERVA CANCELADA", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            refrescarReservas();
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void limpiar() {
        model.setCurrent(null);
    }

    private void imprimir() {
        DefaultTableModel tableModel = view.getTableModel();
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view.getPanel(),
                    "No hay reservas para imprimir.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] columnas = {"ID", "Funcionario", "Actividad", "Fecha", "Hora Inicio", "Hora Fin", "Recursos"};
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
            new GeneradorPDF().generar("reservas.pdf", "Listado de Reservas", columnas, filas);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel(),
                    "No se pudo generar el PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Sin ir a la red: la fila seleccionada sale de la misma lista que ya tiene el Model. */
    private void cargarSeleccion() {
        int fila = view.getTablaReservas().getSelectedRow();
        if (fila < 0) return;
        int id = Integer.parseInt(view.getTableModel().getValueAt(fila, 0).toString());
        for (Reserva reserva : model.getReservas()) {
            if (reserva.getId() == id) {
                model.setCurrent(reserva);
                return;
            }
        }
    }

    private void usarIA() {
        String texto = JOptionPane.showInputDialog(view.getPanel(), "Describa la reserva:", "Usar IA", JOptionPane.PLAIN_MESSAGE);
        if (texto == null) {
            return;
        }

        texto = texto.trim();
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(view.getPanel(), "Debe escribir una descripción.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            ReservaExtraccion datos = service.extraerConIA(texto);   // se ejecuta en el Backend
            aplicarDatosDeIA(datos);
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void aplicarDatosDeIA(ReservaExtraccion datos) {
        if (datos.getActividad() != null) {
            view.getTxtActividad().setText(datos.getActividad());
        }
        if (datos.getFecha() != null) {
            view.getTxtFecha().setText(datos.getFecha());
        }
        if (datos.getHoraInicio() != null) {
            view.getTxtHoraInicio().setText(datos.getHoraInicio());
        }
        if (datos.getHoraFinal() != null) {
            view.getTxtHoraFin().setText(datos.getHoraFinal());
        }

        List<String> noReconocidas = new ArrayList<>();
        String primeraCoincidencia = null;
        if (datos.getCategoriasRecurso() != null) {
            for (String nombreCategoria : datos.getCategoriasRecurso()) {
                boolean encontrada = false;
                for (Recurso recurso : model.getRecursos()) {
                    if (recurso.getCategoria() != null
                            && recurso.getCategoria().getDescripcion() != null
                            && recurso.getCategoria().getDescripcion().equalsIgnoreCase(nombreCategoria)) {
                        if (primeraCoincidencia == null) {
                            primeraCoincidencia = descripcionRecurso(recurso);
                        }
                        encontrada = true;
                        break;
                    }
                }
                if (!encontrada) {
                    noReconocidas.add(nombreCategoria);
                }
            }
        }
        if (primeraCoincidencia != null) {
            view.getListaRecursos().setSelectedItem(primeraCoincidencia);
        }

        String mensaje = "Datos generados por IA cargados en el formulario. "
                + "Revíselos y corríjalos si es necesario antes de registrar la reserva.";
        if (!noReconocidas.isEmpty()) {
            mensaje += "\nCategorías no reconocidas: " + String.join(", ", noReconocidas);
        }
        JOptionPane.showMessageDialog(view.getPanel(), mensaje, "Usar IA", JOptionPane.INFORMATION_MESSAGE);
    }

    // --------------------------- formulario ---------------------------

    private void avisarDatosInvalidos() {
        JOptionPane.showMessageDialog(view.getPanel(),
                "Revise los campos marcados (pase el mouse sobre ellos para ver el detalle).",
                "Datos inválidos", JOptionPane.WARNING_MESSAGE);
    }

    /** @param conId true al editar (el id viene del formulario); false al crear (lo genera MySQL). */
    private Reserva take(boolean conId) {
        Reserva reserva = new Reserva();
        reserva.setId(conId ? Integer.parseInt(view.getTxtId().getText().trim()) : 0);
        reserva.setFuncionario(view.getFuncionario());
        reserva.setActividad(view.getTxtActividad().getText().trim());
        reserva.setFecha(LocalDate.parse(view.getTxtFecha().getText().trim()));
        reserva.setHoraInicio(LocalTime.parse(view.getTxtHoraInicio().getText().trim()));
        reserva.setHoraFin(LocalTime.parse(view.getTxtHoraFin().getText().trim()));

        Recurso recursoSeleccionado = obtenerRecursoSeleccionado();
        if (recursoSeleccionado != null && recursoSeleccionado.getCategoria() != null) {
            reserva.agregarCategoria(recursoSeleccionado.getCategoria());
        }
        return reserva;
    }

    private Recurso obtenerRecursoSeleccionado() {
        int indice = view.getListaRecursos().getSelectedIndex();
        List<Recurso> recursos = model.getRecursos();
        if (indice < 0 || indice >= recursos.size()) {
            return null;
        }
        return recursos.get(indice);
    }

    private String descripcionRecurso(Recurso recurso) {
        String categoria = recurso.getCategoria() != null ? recurso.getCategoria().getDescripcion() : "";
        return recurso.getNombre() + " (" + categoria + ")";
    }

    /** @param exigeId true al editar: el ID debe existir y ser numérico. Al crear se ignora. */
    private boolean validate(boolean exigeId) {
        boolean valido = true;

        if (exigeId) {
            if (view.getTxtId().getText().trim().isEmpty()) {
                valido = false;
                view.getTxtId().setToolTipText("Seleccione una reserva de la tabla para editarla");
            } else {
                try {
                    Integer.parseInt(view.getTxtId().getText().trim());
                    view.getTxtId().setToolTipText(null);
                } catch (NumberFormatException e) {
                    valido = false;
                    view.getTxtId().setToolTipText("El ID debe ser numérico.");
                }
            }
        } else {
            view.getTxtId().setToolTipText(null);
        }

        if (view.getTxtActividad().getText().trim().isEmpty()) {
            valido = false;
            view.getTxtActividad().setToolTipText("Actividad requerida");
        } else {
            view.getTxtActividad().setToolTipText(null);
        }

        if (view.getTxtFecha().getText().trim().isEmpty()) {
            valido = false;
            view.getTxtFecha().setToolTipText("Fecha requerida");
        } else {
            try {
                LocalDate.parse(view.getTxtFecha().getText().trim());
                view.getTxtFecha().setToolTipText(null);
            } catch (Exception e) {
                valido = false;
                view.getTxtFecha().setToolTipText("Formato: AAAA-MM-DD");
            }
        }

        if (view.getTxtHoraInicio().getText().trim().isEmpty()) {
            valido = false;
            view.getTxtHoraInicio().setToolTipText("Hora de inicio requerida");
        } else {
            try {
                LocalTime.parse(view.getTxtHoraInicio().getText().trim());
                view.getTxtHoraInicio().setToolTipText(null);
            } catch (Exception e) {
                valido = false;
                view.getTxtHoraInicio().setToolTipText("Formato: HH:mm");
            }
        }

        if (view.getTxtHoraFin().getText().trim().isEmpty()) {
            valido = false;
            view.getTxtHoraFin().setToolTipText("Hora de finalización requerida");
        } else {
            try {
                LocalTime.parse(view.getTxtHoraFin().getText().trim());
                view.getTxtHoraFin().setToolTipText(null);
            } catch (Exception e) {
                valido = false;
                view.getTxtHoraFin().setToolTipText("Formato: HH:mm");
            }
        }

        if (obtenerRecursoSeleccionado() == null) {
            valido = false;
            view.getListaRecursos().setToolTipText("Recurso requerido");
        } else {
            view.getListaRecursos().setToolTipText(null);
        }

        if (view.getFuncionario() == null) {
            valido = false;
            view.getLblFuncionario().setToolTipText("Funcionario requerido");
        } else {
            view.getLblFuncionario().setToolTipText(null);
        }

        return valido;
    }
}