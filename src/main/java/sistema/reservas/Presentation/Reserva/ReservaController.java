package sistema.reservas.Presentation.Reserva;

import sistema.reservas.Data.llm.ReservaExtraccion;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Logic.Services.ReservaService;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaController {

    private final ReservaPanel view;
    private final ReservaModel model;
    private final ReservaService reservaService;

    public ReservaController(ReservaPanel view, ReservaModel model, ReservaService reservaService) {
        this.view = view;
        this.model = model;
        this.reservaService = reservaService;

        view.setModel(model);

        view.getBtnNueva().addActionListener(e -> limpiar());
        view.getBtnLimpiar().addActionListener(e -> limpiar());
        view.getBtnReservar().addActionListener(e -> reservar());
        view.getBtnEditar().addActionListener(e -> editar());
        view.getBtnCancelar().addActionListener(e -> cancelar());
        view.getBtnUsarIA().addActionListener(e -> usarIA());
        view.getTablaReservas().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        model.setRecursos(reservaService.listarRecursos());
        model.setReservas(reservaService.listarReservas());
    }

    private void reservar() {
        model.setRecursos(reservaService.listarRecursos()); // por si se creó un recurso nuevo en esta sesión
        if (!validate()) {
            JOptionPane.showMessageDialog(view.getPanel(),
                    "Revise los campos marcados (pase el mouse sobre ellos para ver el detalle).",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Reserva reserva = take();
        try {
            reservaService.crearReserva(reserva);
            JOptionPane.showMessageDialog(view.getPanel(), "RESERVA APLICADA", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            model.setReservas(reservaService.listarReservas());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editar() {
        model.setRecursos(reservaService.listarRecursos());
        if (!validate()) {
            JOptionPane.showMessageDialog(view.getPanel(),
                    "Revise los campos marcados (pase el mouse sobre ellos para ver el detalle).",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Reserva reserva = take();
        try {
            reservaService.modificarReserva(reserva);
            JOptionPane.showMessageDialog(view.getPanel(), "RESERVA MODIFICADA", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            model.setReservas(reservaService.listarReservas());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            reservaService.cancelarReserva(id);
            JOptionPane.showMessageDialog(view.getPanel(), "RESERVA CANCELADA", "", JOptionPane.INFORMATION_MESSAGE);
            model.setCurrent(null);
            model.setReservas(reservaService.listarReservas());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar() {
        model.setCurrent(null);
    }

    private void cargarSeleccion() {
        int fila = view.getTablaReservas().getSelectedRow();
        if (fila < 0) return;
        int id = Integer.parseInt(view.getTableModel().getValueAt(fila, 0).toString());
        model.setCurrent(reservaService.buscarReserva(id));
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
            ReservaExtraccion datos = reservaService.extraerReserva(texto);
            aplicarDatosDeIA(datos);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    private Reserva take() {
        Reserva reserva = new Reserva();
        reserva.setId(Integer.parseInt(view.getTxtId().getText().trim()));
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

    private boolean validate() {
        boolean valido = true;

        if (view.getTxtId().getText().trim().isEmpty()) {
            valido = false;
            view.getTxtId().setToolTipText("ID requerido");
        } else {
            try {
                Integer.parseInt(view.getTxtId().getText().trim());
                view.getTxtId().setToolTipText(null);
            } catch (NumberFormatException e) {
                valido = false;
                view.getTxtId().setToolTipText("El ID debe ser numérico.");
            }
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