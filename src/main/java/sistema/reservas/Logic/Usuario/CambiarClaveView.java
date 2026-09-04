package sistema.reservas.Logic.Usuario;

import javax.swing.*;
import java.awt.*;

public class CambiarClaveView extends JDialog {

    private JPasswordField txtClaveActual;
    private JPasswordField txtClaveNueva;
    private JPasswordField txtClaveNuevaConfirmar;
    private JButton btnConfirmar;
    private JButton btnCancelar;
    private JLabel lblMensaje;

    public CambiarClaveView(Frame owner) {
        super(owner, "Cambiar Clave", true);
        construirInterfaz();
    }

    private void construirInterfaz() {
        setSize(320, 220);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Clave actual:"), gbc);
        gbc.gridx = 1;
        txtClaveActual = new JPasswordField(15);
        add(txtClaveActual, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Clave nueva:"), gbc);
        gbc.gridx = 1;
        txtClaveNueva = new JPasswordField(15);
        add(txtClaveNueva, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Confirmar clave:"), gbc);
        gbc.gridx = 1;
        txtClaveNuevaConfirmar = new JPasswordField(15);
        add(txtClaveNuevaConfirmar, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        lblMensaje = new JLabel(" ");
        lblMensaje.setForeground(Color.RED);
        add(lblMensaje, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnConfirmar = new JButton("Confirmar");
        btnCancelar = new JButton("Cancelar");
        panelBotones.add(btnConfirmar);
        panelBotones.add(btnCancelar);

        gbc.gridy = 4;
        add(panelBotones, gbc);

        btnCancelar.addActionListener(e -> dispose());
    }

    public char[] getClaveActual() {
        return txtClaveActual.getPassword();
    }

    public char[] getClaveNueva() {
        return txtClaveNueva.getPassword();
    }

    public char[] getClaveNuevaConfirmar() {
        return txtClaveNuevaConfirmar.getPassword();
    }

    public JButton getBtnConfirmar() {
        return btnConfirmar;
    }

    public JButton getBtnCancelar() {
        return btnCancelar;
    }

    public void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
    }
}