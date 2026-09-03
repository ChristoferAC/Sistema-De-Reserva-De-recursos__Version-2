package sistema.reservas.Categoria;

import sistema.reservas.view.TablaCrudPanel;

import javax.swing.*;
import java.awt.*;

public class CategoriaPanel extends TablaCrudPanel {

    private JTextField txtBuscarDescripcion;
    private JButton btnBuscar;
    private JButton btnImprimir;

    private JTextField txtId;
    private JTextField txtDescripcion;

    private JButton btnGuardar;
    private JButton btnBorrar;
    private JButton btnLimpiar;

    public CategoriaPanel() {
        super(new String[]{"ID", "Descripcion"});
        construirExtras();
    }

    private void construirExtras() {
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("Busqueda"));
        panelBusqueda.add(new JLabel("Descripcion:"));
        txtBuscarDescripcion = new JTextField(15);
        panelBusqueda.add(txtBuscarDescripcion);
        btnBuscar = new JButton("Buscar");
        panelBusqueda.add(btnBuscar);
        btnImprimir = new JButton("Imprimir");
        panelBusqueda.add(btnImprimir);

        JPanel panelForm = new JPanel(new GridBagLayout());
        panelForm.setBorder(BorderFactory.createTitledBorder("Categoria"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelForm.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1;
        txtId = new JTextField(10);
        txtId.setEditable(false);
        panelForm.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panelForm.add(new JLabel("Descripcion:"), gbc);
        gbc.gridx = 1;
        txtDescripcion = new JTextField(25);
        panelForm.add(txtDescripcion, gbc);

        JPanel panelBotonesForm = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnGuardar = new JButton("Guardar");
        btnBorrar = new JButton("Borrar");
        btnLimpiar = new JButton("Limpiar");
        panelBotonesForm.add(btnGuardar);
        panelBotonesForm.add(btnBorrar);
        panelBotonesForm.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panelForm.add(panelBotonesForm, gbc);

        JPanel panelNorte = new JPanel();
        panelNorte.setLayout(new BoxLayout(panelNorte, BoxLayout.Y_AXIS));
        panelNorte.add(panelBusqueda);
        panelNorte.add(panelForm);

        add(panelNorte, BorderLayout.NORTH);

        btnNuevo.setVisible(false);
        btnEditar.setVisible(false);
        btnEliminar.setVisible(false);
        btnActualizar.setVisible(false);
    }

    public JTextField getTxtBuscarDescripcion() { return txtBuscarDescripcion; }
    public JButton getBtnBuscar() { return btnBuscar; }
    public JButton getBtnImprimir() { return btnImprimir; }

    public JTextField getTxtId() { return txtId; }
    public JTextField getTxtDescripcion() { return txtDescripcion; }

    public JButton getBtnGuardar() { return btnGuardar; }
    public JButton getBtnBorrar() { return btnBorrar; }
    public JButton getBtnLimpiar() { return btnLimpiar; }

    public void limpiarFormulario() {
        txtId.setText("");
        txtDescripcion.setText("");
    }

    public void cargarFormulario(int id, String descripcion) {
        txtId.setText(String.valueOf(id));
        txtDescripcion.setText(descripcion);
    }
}