package sistema.reservas.Presentation.Recurso;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RecursoPanel extends JPanel {
    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JComboBox<String> cmbCategoria;
    private JTextField txtFiltroCategoria;
    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnCancelar;
    private JButton btnBuscar;
    private JTable tabla;
    private JScrollPane scrollTabla;

    public RecursoPanel() {
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
        panel.setBorder(BorderFactory.createTitledBorder("Datos del recurso"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        // ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        panel.add(new JLabel("ID / Activo:"), gbc);
        txtId = new JTextField(15);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtId, gbc);

        // Nombre
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Nombre:"), gbc);
        txtNombre = new JTextField(15);
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        panel.add(txtNombre, gbc);

        // Descripción
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Descripción:"), gbc);
        txtDescripcion = new JTextField(15);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtDescripcion, gbc);

        // Categoría
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Categoría:"), gbc);
        cmbCategoria = new JComboBox<>();
        gbc.gridx = 3;
        gbc.weightx = 1.0;
        panel.add(cmbCategoria, gbc);

        // Filtro
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(new JLabel("Filtrar categoría:"), gbc);
        txtFiltroCategoria = new JTextField(15);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(txtFiltroCategoria, gbc);
        btnBuscar = new JButton("Buscar");
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        panel.add(btnBuscar, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnNuevo = new JButton("Nuevo");
        btnGuardar = new JButton("Guardar");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnNuevo);
        panelBotones.add(btnGuardar);
        panelBotones.add(btnEditar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnCancelar);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        panel.add(panelBotones, gbc);

        return panel;
    }

    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Recursos registrados"));
        String[] columnas = {"ID", "Nombre", "Descripción", "Categoría"};

        tabla = new JTable(new javax.swing.table.DefaultTableModel(new Object[][]{}, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        return panel;
    }

    private void configurarEventos() {

        btnNuevo.addActionListener(e -> limpiarFormulario());

        btnCancelar.addActionListener(e -> limpiarFormulario());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                return;
            }

            txtId.setText(String.valueOf(tabla.getValueAt(fila, 0)));

            txtNombre.setText(String.valueOf(tabla.getValueAt(fila, 1)));

            txtDescripcion.setText(String.valueOf(tabla.getValueAt(fila, 2)));
        });

        btnGuardar.addActionListener(e -> {
            if (!validarCampos()) {
                return;
            }

            JOptionPane.showMessageDialog(this, "Datos listos para guardar.", "Recurso", JOptionPane.INFORMATION_MESSAGE);
        });

        btnEditar.addActionListener(e -> {

            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                mostrarMensaje("Seleccione un recurso de la tabla.");
                return;
            }

            if (!validarCampos()) {
                return;
            }

            JOptionPane.showMessageDialog(this, "Datos listos para modificar.", "Recurso", JOptionPane.INFORMATION_MESSAGE);
        });

        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                mostrarMensaje("Seleccione un recurso de la tabla.");
                return;
            }

            int respuesta = JOptionPane.showConfirmDialog(this, "¿Desea eliminar el recurso seleccionado?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

            if (respuesta == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Recurso listo para eliminar.", "Recurso", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnBuscar.addActionListener(e -> {

            String filtro = txtFiltroCategoria.getText().trim();

            if (filtro.isEmpty()) { mostrarMensaje("Ingrese una categoría para filtrar.");
                return;
            }

            JOptionPane.showMessageDialog(this, "Filtro aplicado: " + filtro, "Buscar", JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    private boolean validarCampos() {

        if (txtId.getText().trim().isEmpty()) {
            mostrarMensaje("Debe ingresar el ID del recurso.");
            return false;
        }

        if (txtNombre.getText().trim().isEmpty()) {
            mostrarMensaje("Debe ingresar el nombre del recurso.");
            return false;
        }

        if (txtDescripcion.getText().trim().isEmpty()) {
            mostrarMensaje("Debe ingresar la descripción del recurso.");
            return false;
        }

        if (cmbCategoria.getSelectedItem() == null) {
            mostrarMensaje("Debe seleccionar una categoría.");
            return false;
        }
        return true;
    }

    private void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    public JTextField getTxtId() {return txtId;}

    public JTextField getTxtNombre() {return txtNombre;}

    public JTextField getTxtDescripcion() {return txtDescripcion;}

    public JComboBox<String> getCmbCategoria() {return cmbCategoria;}

    public JTextField getTxtFiltroCategoria() {return txtFiltroCategoria;}

    public JButton getBtnNuevo() {return btnNuevo;}

    public JButton getBtnGuardar() {return btnGuardar;}

    public JButton getBtnEditar() {return btnEditar;}

    public JButton getBtnEliminar() {return btnEliminar;}

    public JButton getBtnCancelar() {return btnCancelar;}

    public JButton getBtnBuscar() {return btnBuscar;}

    public JTable getTabla() {return tabla;}

    // UTILITIES

    public void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");

        if (cmbCategoria.getItemCount() > 0) {
            cmbCategoria.setSelectedIndex(0);
        }
        tabla.clearSelection();
    }

    public void limpiarTabla() {
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tabla.getModel();
        modelo.setRowCount(0);
    }

    public void agregarFilaTabla(String id, String nombre, String descripcion, String categoria) {
        javax.swing.table.DefaultTableModel modelo = (javax.swing.table.DefaultTableModel) tabla.getModel();

        modelo.addRow(new Object[]{ id, nombre, descripcion, categoria});
    }

    public int getFilaSeleccionada() {return tabla.getSelectedRow();}


}
