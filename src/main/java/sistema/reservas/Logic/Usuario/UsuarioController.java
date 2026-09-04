package sistema.reservas.Logic.Usuario;

import sistema.reservas.Presentation.Login.LoginView;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class UsuarioController {

    private final LoginView view;
    private final UsuarioService usuarioService;
    private final Consumer<Usuario> onLoginExitoso;

    public UsuarioController(LoginView view, UsuarioService usuarioService,
                             Consumer<Usuario> onLoginExitoso) {
        this.view = view;
        this.usuarioService = usuarioService;
        this.onLoginExitoso = onLoginExitoso;

        this.view.getBtnIngresar().addActionListener(e -> intentarLogin());
        this.view.getBtnCambiar().addActionListener(e -> abrirCambiarClave());
    }

    private void intentarLogin() {
        String username = view.getUsuario();
        String password = new String(view.getPassword());

        Usuario usuario = usuarioService.login(username, password);

        if (usuario == null) {
            view.mostrarMensaje("Usuario o clave incorrectos.");
            return;
        }

        view.mostrarMensaje(" ");
        onLoginExitoso.accept(usuario);
    }

    private void abrirCambiarClave() {
        String username = view.getUsuario();
        if (username == null || username.isBlank()) {
            view.mostrarMensaje("Ingrese su usuario antes de cambiar la clave.");
            return;
        }

        Usuario usuario = usuarioService.login(username, new String(view.getPassword()));
        if (usuario == null) {
            view.mostrarMensaje("Ingrese su usuario y clave actual antes de cambiarla.");
            return;
        }

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(view);
        CambiarClaveView dialog = new CambiarClaveView(owner);

        dialog.getBtnConfirmar().addActionListener(e -> {
            String claveActual = new String(dialog.getClaveActual());
            String claveNueva = new String(dialog.getClaveNueva());
            String claveConfirmar = new String(dialog.getClaveNuevaConfirmar());

            if (!claveNueva.equals(claveConfirmar)) {
                dialog.mostrarMensaje("Las claves nuevas no coinciden.");
                return;
            }

            try {
                usuarioService.cambiarClave(usuario, claveActual, claveNueva);
                dialog.mostrarMensaje(" ");
                JOptionPane.showMessageDialog(dialog, "Clave actualizada correctamente.");
                dialog.dispose();
            } catch (IllegalArgumentException ex) {
                dialog.mostrarMensaje(ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }
}