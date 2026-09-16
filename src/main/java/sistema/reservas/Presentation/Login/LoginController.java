package sistema.reservas.Presentation.Login;

import sistema.reservas.Logic.Sesion;
import sistema.reservas.Logic.Usuario;
import sistema.reservas.Logic.Services.UsuarioService;
import sistema.reservas.Presentation.CambiarClave.CambiarClaveController;
import sistema.reservas.Presentation.CambiarClave.CambiarClaveModel;
import sistema.reservas.Presentation.CambiarClave.CambiarClaveView;

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

    public Usuario login(String username, String password) throws Exception {
        Usuario usuario = usuarioService.login(username, password);
        Sesion.setUsuario(usuario);
        return usuario;
    }

    private void abrirCambiarClave() {
        // Se abre directo sin validar nada — el dialog tiene su propio
        // campo de usuario y clave actual, y su propio Controller valida
        CambiarClaveModel cambiarModel = new CambiarClaveModel();
        CambiarClaveView  cambiarView  = new CambiarClaveView(view);
        new CambiarClaveController(cambiarView, cambiarModel);

        cambiarView.setVisible(true);
    }
}