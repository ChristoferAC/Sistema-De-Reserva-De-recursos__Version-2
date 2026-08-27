package sistema.reservas.controller;

import sistema.reservas.model.Usuario;
import sistema.reservas.service.UsuarioService;
import sistema.reservas.view.LoginView;

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
}