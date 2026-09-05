package sistema.reservas.Presentation.Reserva;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaPanel implements PropertyChangeListener {

    private JPanel panel1;
    private JLabel lblFuncionario;
    private JTextField txtId;
    private JTextField txtActividad;
    private JTextField txtFecha;
    private JTextField txtHoraInicio;
    private JTextField txtHoraFin;
    private JComboBox<String> listaCategorias;
    private JButton btnNueva;
    private JButton btnReservar;
    private JButton btnEditar;
    private JButton btnCancelar;
    private JButton btnLimpiar;
    private JButton btnUsarIA;
    private JTable tablaReservas;

    private Funcionario funcionarioActual;

    /** Objetos reales que respaldan las descripciones mostradas en listaCategorias (mismo orden). */
    private final List<CategoriaRecurso> categoriasDisponibles = new ArrayList<>();

    public ReservaPanel() {

        btnNueva.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiar();
            }
        });

        btnLimpiar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiar();
            }
        });

        btnReservar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validate()) {
                    Reserva reserva = take();
                    try {
                        controller.crear(reserva);
                        JOptionPane.showMessageDialog(panel1,
                                "RESERVA APLICADA", "", JOptionPane.INFORMATION_MESSAGE);
                        limpiar();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel1, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnEditar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validate()) {
                    Reserva reserva = take();
                    try {
                        controller.modificar(reserva);
                        JOptionPane.showMessageDialog(panel1, "RESERVA MODIFICADA", "", JOptionPane.INFORMATION_MESSAGE);
                        limpiar();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel1, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int fila = tablaReservas.getSelectedRow();
                if (fila < 0) {
                    JOptionPane.showMessageDialog(panel1,
                            "Seleccione una reserva.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                int respuesta = JOptionPane.showConfirmDialog(panel1,
                        "¿Desea cancelar la reserva seleccionada?",
                        "Confirmar cancelación", JOptionPane.YES_NO_OPTION);

                if (respuesta != JOptionPane.YES_OPTION) {
                    return;
                }

                try {
                    int id = Integer.parseInt(tablaReservas.getValueAt(fila, 0).toString());
                    controller.cancelar(id);
                    JOptionPane.showMessageDialog(panel1,
                            "RESERVA CANCELADA", "", JOptionPane.INFORMATION_MESSAGE);
                    limpiar();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel1, ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnUsarIA.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String texto = JOptionPane.showInputDialog(panel1,
                        "Describa la reserva:", "Usar IA", JOptionPane.PLAIN_MESSAGE);

                if (texto == null) {
                    return;
                }

                texto = texto.trim();
                if (texto.isEmpty()) {
                    JOptionPane.showMessageDialog(panel1,
                            "Debe escribir una descripción.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // La integración con IA se conecta aquí posteriormente.
            }
        });

        tablaReservas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarReservaSeleccionada();
            }
        });
    }

    public JPanel getPanel() {
        return panel1;
    }

    ReservaController controller;
    ReservaService service;

    public void setController(ReservaController controller) {
        this.controller = controller;
        cargarReservas();
    }

    public void setService(ReservaService service) {
        this.service = service;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionarioActual = funcionario;
        lblFuncionario.setText(funcionario != null ? funcionario.getNombre() : "");
    }

    public Funcionario getFuncionario() {
        return funcionarioActual;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        cargarReservas();
        panel1.revalidate();
        panel1.repaint();
    }

    public Reserva take() {
        Reserva reserva = new Reserva();
        reserva.setId(Integer.parseInt(txtId.getText().trim()));
        reserva.setFuncionario(funcionarioActual);
        reserva.setActividad(txtActividad.getText().trim());
        reserva.setFecha(LocalDate.parse(txtFecha.getText().trim()));
        reserva.setHoraInicio(LocalTime.parse(txtHoraInicio.getText().trim()));
        reserva.setHoraFin(LocalTime.parse(txtHoraFin.getText().trim()));

        CategoriaRecurso categoriaSeleccionada = obtenerCategoriaSeleccionada();
        if (categoriaSeleccionada != null) {
            reserva.agregarCategoria(categoriaSeleccionada);
        }
        return reserva;
    }

    /** Resuelve el item seleccionado en el combo hacia el objeto real que representa. */
    private CategoriaRecurso obtenerCategoriaSeleccionada() {
        int indice = listaCategorias.getSelectedIndex();
        if (indice < 0 || indice >= categoriasDisponibles.size()) {
            return null;
        }
        return categoriasDisponibles.get(indice);
    }

    private boolean validate() {
        boolean valido = true;

        if (txtId.getText().trim().isEmpty()) {
            valido = false;
            txtId.setToolTipText("ID requerido");
        } else {
            try {
                Integer.parseInt(txtId.getText().trim());
                txtId.setToolTipText(null);
            } catch (NumberFormatException e) {
                valido = false;
                txtId.setToolTipText("El ID debe ser numérico.");
            }
        }

        if (txtActividad.getText().trim().isEmpty()) {
            valido = false;
            txtActividad.setToolTipText("Actividad requerida");
        } else {
            txtActividad.setToolTipText(null);
        }

        if (txtFecha.getText().trim().isEmpty()) {
            valido = false;
            txtFecha.setToolTipText("Fecha requerida");
        } else {
            try {
                LocalDate.parse(txtFecha.getText().trim());
                txtFecha.setToolTipText(null);
            } catch (Exception e) {
                valido = false;
                txtFecha.setToolTipText("Formato: AAAA-MM-DD");
            }
        }

        if (txtHoraInicio.getText().trim().isEmpty()) {
            valido = false;
            txtHoraInicio.setToolTipText("Hora de inicio requerida");
        } else {
            try {
                LocalTime.parse(txtHoraInicio.getText().trim());
                txtHoraInicio.setToolTipText(null);
            } catch (Exception e) {
                valido = false;
                txtHoraInicio.setToolTipText("Formato: HH:mm");
            }
        }

        if (txtHoraFin.getText().trim().isEmpty()) {
            valido = false;
            txtHoraFin.setToolTipText("Hora de finalización requerida");
        } else {
            try {
                LocalTime.parse(txtHoraFin.getText().trim());
                txtHoraFin.setToolTipText(null);
            } catch (Exception e) {
                valido = false;
                txtHoraFin.setToolTipText("Formato: HH:mm");
            }
        }

        if (listaCategorias.getSelectedItem() == null) {
            valido = false;
            listaCategorias.setToolTipText("Categoría requerida");
        } else {
            listaCategorias.setToolTipText(null);
        }

        if (funcionarioActual == null) {
            valido = false;
            lblFuncionario.setToolTipText("Funcionario requerido");
        } else {
            lblFuncionario.setToolTipText(null);
        }

        return valido;
    }

    private void cargarReservas() {
        if (controller == null) {
            return;
        }

        try {
            List<Reserva> reservas = controller.listar();

            String[] columnas = {"ID", "Funcionario", "Actividad", "Fecha", "Hora Inicio", "Hora Fin", "Recursos"};

            DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Reserva reserva : reservas) {
                String nombreFuncionario = reserva.getFuncionario() != null
                        ? reserva.getFuncionario().getNombre() : "";

                modelo.addRow(new Object[]{
                        reserva.getId(),
                        nombreFuncionario,
                        reserva.getActividad(),
                        reserva.getFecha(),
                        reserva.getHoraInicio(),
                        reserva.getHoraFin(),
                        obtenerRecursos(reserva)
                });
            }

            tablaReservas.setModel(modelo);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel1, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String obtenerRecursos(Reserva reserva) {
        if (reserva.getRecursosAsignados() == null) {
            return "";
        }

        StringBuilder texto = new StringBuilder();
        List<Recurso> recursos = reserva.getRecursosAsignados();

        for (int i = 0; i < recursos.size(); i++) {
            Recurso recurso = recursos.get(i);
            if (recurso == null) {
                continue;
            }
            texto.append(recurso.getNombre());
            if (i < recursos.size() - 1) {
                texto.append(", ");
            }
        }

        return texto.toString();
    }

    private void cargarReservaSeleccionada() {
        int fila = tablaReservas.getSelectedRow();
        if (fila < 0) {
            return;
        }

        txtId.setText(String.valueOf(tablaReservas.getValueAt(fila, 0)));
        lblFuncionario.setText(String.valueOf(tablaReservas.getValueAt(fila, 1)));
        txtActividad.setText(String.valueOf(tablaReservas.getValueAt(fila, 2)));
        txtFecha.setText(String.valueOf(tablaReservas.getValueAt(fila, 3)));
        txtHoraInicio.setText(String.valueOf(tablaReservas.getValueAt(fila, 4)));
        txtHoraFin.setText(String.valueOf(tablaReservas.getValueAt(fila, 5)));
    }

    public void limpiar() {
        txtId.setText("");
        txtActividad.setText("");
        txtFecha.setText("");
        txtHoraInicio.setText("");
        txtHoraFin.setText("");
        listaCategorias.setSelectedItem(null);
        tablaReservas.clearSelection();
    }

    /** Agrega una categoría real disponible; su descripción es lo que se muestra en el combo. */
    public void agregarCategoria(CategoriaRecurso categoria) {
        if (categoria == null || categoria.getDescripcion() == null || categoria.getDescripcion().trim().isEmpty()) {
            return;
        }
        categoriasDisponibles.add(categoria);
        listaCategorias.addItem(categoria.getDescripcion());
    }

    public void limpiarCategorias() {
        categoriasDisponibles.clear();
        listaCategorias.removeAllItems();
    }

    public CategoriaRecurso getCategoriaSeleccionada() {
        return obtenerCategoriaSeleccionada();
    }
}