package sistema.reservas.Presentation.Reserva;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

    /**
     * Objetos reales que respaldan las descripciones mostradas en listaCategorias (mismo orden).
     */
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

    /**
     * Resuelve el item seleccionado en el combo hacia el objeto real que representa.
     */
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

    /**
     * Agrega una categoría real disponible; su descripción es lo que se muestra en el combo.
     */
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "Nueva Reserva", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.BELOW_TOP, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Funcionario:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        lblFuncionario = new JLabel();
        lblFuncionario.setText("");
        panel1.add(lblFuncionario, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("ID Reserva:");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtId = new JTextField();
        panel1.add(txtId, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Fecha:");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Hora Finalizacion:");
        panel1.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtFecha = new JTextField();
        txtFecha.setText("");
        panel1.add(txtFecha, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtHoraFin = new JTextField();
        panel1.add(txtHoraFin, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Actividad:");
        panel1.add(label5, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Hora Inicio:");
        panel1.add(label6, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Categorías:");
        panel1.add(label7, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtActividad = new JTextField();
        panel1.add(txtActividad, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtHoraInicio = new JTextField();
        panel1.add(txtHoraInicio, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 7, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(4, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnNueva = new JButton();
        btnNueva.setText("Nueva");
        panel2.add(btnNueva, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnReservar = new JButton();
        btnReservar.setText("Registrar reserva");
        panel2.add(btnReservar, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEditar = new JButton();
        btnEditar.setText("Modificar");
        panel2.add(btnEditar, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCancelar = new JButton();
        btnCancelar.setText("Cancelar Reserva");
        panel2.add(btnCancelar, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLimpiar = new JButton();
        btnLimpiar.setText("Limpiar");
        panel2.add(btnLimpiar, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnUsarIA = new JButton();
        btnUsarIA.setText("Usar IA");
        panel2.add(btnUsarIA, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listaCategorias = new JComboBox();
        panel1.add(listaCategorias, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(5, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(null, "Reservas", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tablaReservas = new JTable();
        scrollPane1.setViewportView(tablaReservas);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}