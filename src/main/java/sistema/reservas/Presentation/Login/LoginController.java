package sistema.reservas.Presentation.Login;

import sistema.reservas.Logic.Sesion;
import sistema.reservas.Logic.Usuario;
import sistema.reservas.Presentation.CambiarClave.CambiarClaveController;
import sistema.reservas.Presentation.CambiarClave.CambiarClaveModel;
import sistema.reservas.Presentation.CambiarClave.CambiarClaveView;
import sistema.reservas.Logic.Services.UsuarioService;

public class LoginController {

    private final LoginView view;
    private final LoginModel model;
    private final UsuarioService usuarioService;

    public LoginController(LoginView view, LoginModel model) {
        this.view           = view;
        this.model          = model;
        this.usuarioService = new UsuarioService();

        view.setController(this);
        view.setModel(model);

        view.getBtnCambiar().addActionListener(e -> abrirCambiarClave());
    }

    /**
     * Valida credenciales y guarda el usuario en Sesion.
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

        // Crear el MVC completo de CambiarClave y abrir el dialog
        CambiarClaveModel cambiarModel = new CambiarClaveModel();
        CambiarClaveView  cambiarView  = new CambiarClaveView(view);
        new CambiarClaveController(cambiarView, cambiarModel, usuario);

        cambiarView.setVisible(true);
    }
}