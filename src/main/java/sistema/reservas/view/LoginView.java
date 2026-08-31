package sistema.reservas.view;

import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar;
    private JButton btnCambiar;
    private JLabel lblMensaje;

    public LoginView() {
        super("Sistema de Reserva de Recursos - Login");
        construirInterfaz();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(340, 200);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        txtUsuario = new JTextField(15);
        add(txtUsuario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Contraseña:"), gbc);
        gbc.gridx = 1;
        txtPassword = new JPasswordField(15);
        add(txtPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        lblMensaje = new JLabel(" ");
        lblMensaje.setForeground(Color.RED);
        add(lblMensaje, gbc);

        // Fila de botones: un solo "Ingresar" (columna 0) y "Cambiar clave" (columna 1).
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        btnIngresar = new JButton("Ingresar");
        add(btnIngresar, gbc);

        gbc.gridx = 1;
        btnCambiar = new JButton("Cambiar clave");
        add(btnCambiar, gbc);
    }

    public String getUsuario() {
        return txtUsuario.getText();
    }

    public char[] getPassword() {
        return txtPassword.getPassword();
    }

    public JButton getBtnIngresar() {
        return btnIngresar;
    }

    public JButton getBtnCambiar() {
        return btnCambiar;
    }

    /** Para que el controller muestre errores de login sin usar JOptionPane. */
    public void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
    }
}