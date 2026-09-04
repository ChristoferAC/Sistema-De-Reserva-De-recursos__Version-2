package sistema.reservas.Presentation.Reserva;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ReservaPanel extends JPanel {

    private JTextField txtId;
    private JTextField txtActividad;
    private JTextField txtFecha;
    private JTextField txtHoraInicio;
    private JTextField txtHoraFin;
    private JLabel lblFuncionario;
    private JList<String> listaCategorias;
    private DefaultListModel<String> modeloCategorias;
    private JButton btnNueva;
    private JButton btnReservar;
    private JButton btnEditar;
    private JButton btnCancelar;
    private JButton btnLimpiar;
    private JButton btnUsarIA;
    private JTable tablaReservas;

    public ReservaPanel() {
        super(new BorderLayout(10, 10));
        construirInterfaz();
        configurarEventos();
    }

    private void construirInterfaz() {

        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));

        panelPrincipal.add(crearPanelFormulario(), BorderLayout.NORTH);

        panelPrincipal.add(crearPanelTabla(), BorderLayout.CENTER);

        add(panelPrincipal, BorderLayout.CENTER);
    }

    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBorder(BorderFactory.createTitledBorder("Nueva reserva"));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 1.0;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        panel.add(new JLabel("Funcionario:"), gbc);

        lblFuncionario = new JLabel("Usuario actual");

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;

        panel.add(lblFuncionario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;

        panel.add(new JLabel("ID reserva:"), gbc);

        txtId = new JTextField(10);
        gbc.gridx = 1;
        gbc.weightx = 1.0;

        panel.add(txtId, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;

        panel.add(new JLabel("Actividad:"), gbc);

        txtActividad = new JTextField(20);

        gbc.gridx = 3;
        gbc.weightx = 1.0;

        panel.add(txtActividad, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;

        panel.add(new JLabel("Fecha:"), gbc);

        txtFecha = new JTextField(10);

        txtFecha.setToolTipText("Formato: AAAA-MM-DD");

        gbc.gridx = 1;
        gbc.weightx = 1.0;

        panel.add(txtFecha, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;

        panel.add(new JLabel("Hora inicio:"), gbc);

        txtHoraInicio = new JTextField(10);

        txtHoraInicio.setToolTipText("Formato: HH:mm");

        gbc.gridx = 3;
        gbc.weightx = 1.0;

        panel.add(txtHoraInicio, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;

        panel.add(new JLabel("Hora finalización:"), gbc);

        txtHoraFin = new JTextField(10);

        txtHoraFin.setToolTipText("Formato: HH:mm");

        gbc.gridx = 1;
        gbc.weightx = 1.0;

        panel.add(txtHoraFin, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;

        panel.add(new JLabel("Categorías:"), gbc);

        modeloCategorias = new DefaultListModel<>();

        listaCategorias = new JList<>(modeloCategorias);

        listaCategorias.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        listaCategorias.setVisibleRowCount(4);

        JScrollPane scrollCategorias = new JScrollPane(listaCategorias);

        gbc.gridx = 3;
        gbc.weightx = 1.0;

        panel.add(scrollCategorias, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnNueva = new JButton("Nueva");

        btnReservar = new JButton("Registrar reserva");

        btnEditar = new JButton("Modificar");

        btnCancelar = new JButton("Cancelar reserva");

        btnLimpiar = new JButton("Limpiar");

        btnUsarIA = new JButton("Usar IA");

        panelBotones.add(btnNueva);
        panelBotones.add(btnReservar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnUsarIA);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;

        panel.add(panelBotones, gbc);
        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Reservas"));
        String[] columnas = {"ID", "Funcionario", "Actividad", "Fecha", "Hora inicio", "Hora fin", "Recursos"};
        javax.swing.table.DefaultTableModel modelo = new javax.swing.table.DefaultTableModel(columnas,0) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaReservas = new JTable(modelo);
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(tablaReservas), BorderLayout.CENTER);
        return panel;
    }
    private void configurarEventos() {
        btnNueva.addActionListener(e -> limpiarFormulario());

        btnLimpiar.addActionListener(e -> limpiarFormulario());

        btnCancelar.addActionListener(e -> cancelarReservaVisual());

        tablaReservas.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            cargarFilaSeleccionada();
        });

        btnReservar.addActionListener(e -> {
            if (!validarCampos()) {
                return;
            }
            JOptionPane.showMessageDialog(this, "Los datos están listos para enviar al Controller.", "Reserva", JOptionPane.INFORMATION_MESSAGE);
        });

        btnEditar.addActionListener(e -> {
            if (tablaReservas.getSelectedRow() < 0) {
                mostrarMensaje("Seleccione una reserva.");
                return;
            }

            if (!validarCampos()) {
                return;
            }

            JOptionPane.showMessageDialog(this, "La reserva está lista para modificar.", "Reserva", JOptionPane.INFORMATION_MESSAGE);
        });
        btnUsarIA.addActionListener(e -> abrirEntradaIA());
    }

    private void abrirEntradaIA() {
        JTextArea areaTexto = new JTextArea(6, 40);

        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);

        JScrollPane scroll = new JScrollPane(areaTexto);

        int respuesta = JOptionPane.showConfirmDialog(this, scroll,"Describir reserva mediante IA", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (respuesta == JOptionPane.OK_OPTION) {

            String texto = areaTexto.getText().trim();
            if (texto.isEmpty()) {
                mostrarMensaje("Debe escribir una descripción.");
                return;
            }

            /*
             * Posteriormente:
             *
             * texto
             *    ↓
             * LLMService
             *    ↓
             * ReservaDTO
             *    ↓
             * llenar formulario
             */
            mostrarMensaje("No he hecho esta parte, perdonasai.");
        }
    }
    private void cancelarReservaVisual() {

        int fila = tablaReservas.getSelectedRow();

        if (fila < 0) {
            mostrarMensaje("Seleccione una reserva.");
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea cancelar la reserva seleccionada?", "Confirmar cancelación", JOptionPane.YES_NO_OPTION);

        if (respuesta == JOptionPane.YES_OPTION) {

            JOptionPane.showMessageDialog(this, "La reserva está lista para ser cancelada mediante el Controller.", "Reserva", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    public boolean validarCampos() {
        if (txtId.getText().trim().isEmpty()) {
            mostrarMensaje("Debe indicar el ID de la reserva.");
            return false;
        }

        if (txtActividad.getText().trim().isEmpty()) {
            mostrarMensaje("Debe indicar la actividad.");
            return false;
        }

        if (txtFecha.getText().trim().isEmpty()) {
            mostrarMensaje("Debe indicar la fecha.");
            return false;
        }

        if (txtHoraInicio.getText().trim().isEmpty()) {
            mostrarMensaje("Debe indicar la hora de inicio.");
            return false;
        }

        if (txtHoraFin.getText().trim().isEmpty()) {

            mostrarMensaje("Debe indicar la hora de finalización.");
            return false;
        }

        if (listaCategorias.getSelectedValuesList().isEmpty()) {
            mostrarMensaje("Debe seleccionar al menos una categoría.");
            return false;
        }
        return true;
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    public JTextField getTxtId() {return txtId;}

    public JTextField getTxtActividad() {return txtActividad;}

    public JTextField getTxtFecha() {return txtFecha;}

    public JTextField getTxtHoraInicio() {return txtHoraInicio;}

    public JTextField getTxtHoraFin() {return txtHoraFin;}

    public JLabel getLblFuncionario() {return lblFuncionario;}

    public JList<String> getListaCategorias() {return listaCategorias;}

    public DefaultListModel<String> getModeloCategorias() {return modeloCategorias;}

    public JButton getBtnNueva() {return btnNueva;}

    public JButton getBtnReservar() {return btnReservar;}

    public JButton getBtnEditar() {return btnEditar;}

    public JButton getBtnCancelar() {return btnCancelar;}

    public JButton getBtnLimpiar() {return btnLimpiar;}

    public JButton getBtnUsarIA() {return btnUsarIA;}

    public JTable getTablaReservas() {return tablaReservas;}

    // UTILITIES

    public void limpiarFormulario() {
        txtId.setText("");
        txtActividad.setText("");
        txtFecha.setText("");
        txtHoraInicio.setText("");
        txtHoraFin.setText("");
        listaCategorias.clearSelection();
        tablaReservas.clearSelection();
    }

    public void agregarCategoria(String categoria) {
        if (categoria != null && !categoria.trim().isEmpty()) {
            modeloCategorias.addElement(categoria.trim());
        }

    }

    public void limpiarCategorias() {
        modeloCategorias.clear();
    }

    public String[] obtenerCategoriasSeleccionadas() {
        return listaCategorias.getSelectedValuesList().toArray(new String[0]);
    }

    public int getFilaSeleccionada() {
        return tablaReservas.getSelectedRow();
    }

    public void agregarFilaReserva(String id, String funcionario, String actividad, String fecha, String horaInicio, String horaFin, String recursos) {
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel)
                tablaReservas.getModel();
        modelo.addRow(new Object[] {id, funcionario, actividad, fecha, horaInicio,horaFin,recursos} );
    }

    public void limpiarTabla() {
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel)
                tablaReservas.getModel();
        modelo.setRowCount(0);
    }

    private void cargarFilaSeleccionada(){
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
}