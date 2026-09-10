package sistema.reservas.Presentation.Usuario;

import sistema.reservas.Logic.Sesion;
import sistema.reservas.Logic.Usuario;
import sistema.reservas.Presentation.CambiarClave.CambiarClaveView;

import javax.swing.*;

public class UsuarioController {

    private final LoginView view;
    private final LoginModel model;
    private final UsuarioService usuarioService;

    public UsuarioController(LoginView view, LoginModel model) {
        this.view = view;
        this.model = model;
        this.usuarioService = new UsuarioService();

        view.setController(this);
        view.setModel(model);

        view.getBtnCambiar().addActionListener(e -> abrirCambiarClave());
    }

    /**
     * Valida el usuario y clave contra el UsuarioService y, si son
     * correctos, guarda el usuario en la Sesion.
     * @return el Usuario logueado.
     * @throws Exception si el usuario o la clave son incorrectos.
     */
    public Usuario login(String username, String password) throws Exception {
        Usuario usuario = usuarioService.login(username, password);
        Sesion.setUsuario(usuario);
        return usuario;
    }

    private void abrirCambiarClave() {
        String username = view.getUsuario();
        if (username == null || username.isBlank()) {
            view.mostrarMensaje("Ingrese su usuario antes de cambiar la clave.");
            return;
        }

        Usuario usuario;
        try {
            usuario = usuarioService.login(username, new String(view.getPassword()));
        } catch (Exception ex) {
            view.mostrarMensaje("Ingrese su usuario y clave actual antes de cambiarla.");
            return;
        }

        // view ahora es un JDialog (antes era JFrame), por eso se pasa
        // directamente como dueño en vez de buscarlo con
        // SwingUtilities.getWindowAncestor(...).
        CambiarClaveView dialog = new CambiarClaveView(view);

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
            } catch (Exception ex) {
                dialog.mostrarMensaje(ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }
}