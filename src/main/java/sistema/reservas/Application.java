package sistema.reservas;

import sistema.reservas.controller.UsuarioController;
import sistema.reservas.dao.UsuarioDAO;
import sistema.reservas.dao.UsuarioDAOXml;
import sistema.reservas.model.Administrador;
import sistema.reservas.service.UsuarioService;
import sistema.reservas.view.LoginView;
import sistema.reservas.view.MainWindow;

import javax.swing.SwingUtilities;

public class Application {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Application::iniciarLogin);
    }

    private static void iniciarLogin() {
        UsuarioDAO usuarioDAO = new UsuarioDAOXml();
        sembrarAdministradorInicial(usuarioDAO);

        UsuarioService usuarioService = new UsuarioService(usuarioDAO);

        LoginView loginView = new LoginView();

        new UsuarioController(loginView, usuarioService, usuarioLogueado -> {
            loginView.dispose();
            // Ahora si se le pasa el usuario logueado a MainWindow, para
            // que pueda decidir que pestanas mostrar segun el rol.
            new MainWindow(usuarioLogueado).setVisible(true);
        });

        loginView.setVisible(true);
    }

    /**
     * Si data/usuarios.xml todavia no tiene ningun usuario (primera vez
     * que se corre el programa), crea un administrador por defecto para
     * poder entrar la primera vez. Usuario: admin / Clave: admin.
     */
    private static void sembrarAdministradorInicial(UsuarioDAO usuarioDAO) {
        if (usuarioDAO.listarTodos().isEmpty()) {
            usuarioDAO.guardar(new Administrador(1, "Administrador", "admin", "admin"));
        }
    }
}