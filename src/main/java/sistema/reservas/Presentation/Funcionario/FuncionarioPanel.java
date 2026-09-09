package sistema.reservas.Presentation.Funcionario;

import sistema.reservas.Presentation.Login.TablaCrudPanel;

import javax.swing.*;
import java.awt.*;

public class FuncionarioPanel extends TablaCrudPanel {

    private JTextField txtBuscarId;
    private JTextField txtBuscarNombre;
    private JButton btnBuscar;
    private JButton btnImprimir;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JTextField txtUsername;

    private JButton btnGuardar;
    private JButton btnBorrar;
    private JButton btnLimpiar;

    public FuncionarioPanel() {
        super(new String[]{"ID", "Nombre", "Usuario", "Telefono"});
        construirExtras();
    }

    private void construirExtras() {
        // --- Panel de busqueda ---
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("Busqueda"));
        panelBusqueda.add(new JLabel("ID:"));
        txtBuscarId = new JTextField(6);
        panelBusqueda.add(txtBuscarId);
        panelBusqueda.add(new JLabel("Nombre:"));
        txtBuscarNombre = new JTextField(12);
        panelBusqueda.add(txtBuscarNombre);
        btnBuscar = new JButton("Buscar");
        panelBusqueda.add(btnBuscar);
        btnImprimir = new JButton("Imprimir");
        panelBusqueda.add(btnImprimir);

        // --- Panel de formulario ---
        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder("Funcionario"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelForm.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        txtId = new JTextField(10);
        // El ID de Funcionario NO es autogenerado (a diferencia de
        // Categoria) - el enunciado pide que quien lo crea lo indique.
        panelForm.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panelForm.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panelForm.add(txtNombre, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panelForm.add(new JLabel("Telefono:"), gbc);
        gbc.gridx = 1;
        txtTelefono = new JTextField(20);
        panelForm.add(txtTelefono, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panelForm.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        txtUsername = new JTextField(20);
        panelForm.add(txtUsername, gbc);

        JPanel panelBotonesForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnGuardar = new JButton("Guardar");
        btnBorrar = new JButton("Borrar");
        btnLimpiar = new JButton("Limpiar");
        panelBotonesForm.add(btnGuardar);
        panelBotonesForm.add(btnBorrar);
        panelBotonesForm.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panelForm.add(panelBotonesForm, gbc);

        // --- Ensamblado ---
        JPanel panelNorte = new JPanel();
        panelNorte.setLayout(new BoxLayout(panelNorte, BoxLayout.Y_AXIS));
        panelNorte.add(panelBusqueda);
        panelNorte.add(panelForm);

        add(panelNorte, BorderLayout.NORTH);

        // Los botones genericos de TablaCrudPanel (Nuevo/Editar/Eliminar/Actualizar)
        // no se usan en este panel: el CRUD se maneja con Guardar/Borrar/Limpiar.
        btnNuevo.setVisible(false);
        btnEditar.setVisible(false);
        btnEliminar.setVisible(false);
        btnActualizar.setVisible(false);
    }

    // --- Getters para el controller ---

    public JTextField getTxtBuscarId() { return txtBuscarId; }
    public JTextField getTxtBuscarNombre() { return txtBuscarNombre; }
    public JButton getBtnBuscar() { return btnBuscar; }
    public JButton getBtnImprimir() { return btnImprimir; }

    public JTextField getTxtId() { return txtId; }
    public JTextField getTxtNombre() { return txtNombre; }
    public JTextField getTxtTelefono() { return txtTelefono; }
    public JTextField getTxtUsername() { return txtUsername; }

    public JButton getBtnGuardar() { return btnGuardar; }
    public JButton getBtnBorrar() { return btnBorrar; }
    public JButton getBtnLimpiar() { return btnLimpiar; }

    /** Limpia el formulario para cargar un nuevo funcionario. */
    public void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtUsername.setText("");
    }

    /** Carga los datos de un funcionario seleccionado en el formulario. */
    public void cargarFormulario(int id, String nombre, String username, String telefono) {
        txtId.setText(String.valueOf(id));
        txtNombre.setText(nombre);
        txtUsername.setText(username);
        txtTelefono.setText(telefono);
    }
}