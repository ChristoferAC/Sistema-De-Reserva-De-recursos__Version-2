package sistema.reservas;

import sistema.reservas.controller.CategoriaRecursoController;
import sistema.reservas.controller.FuncionarioController;
import sistema.reservas.controller.UsuarioController;
import sistema.reservas.dao.CategoriaRecursoDAO;
import sistema.reservas.dao.CategoriaRecursoDAOXml;
import sistema.reservas.dao.FuncionarioDAO;
import sistema.reservas.dao.FuncionarioDAOXml;
import sistema.reservas.dao.UsuarioDAO;
import sistema.reservas.dao.UsuarioDAOXml;
import sistema.reservas.model.Administrador;
import sistema.reservas.model.Usuario;
import sistema.reservas.service.CategoriaRecursoService;
import sistema.reservas.service.FuncionarioService;
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
            abrirVentanaPrincipal(usuarioLogueado);
        });

        loginView.setVisible(true);
    }

    private static void abrirVentanaPrincipal(Usuario usuarioLogueado) {
        MainWindow mainWindow = new MainWindow(usuarioLogueado);


        if ("ADMIN".equals(usuarioLogueado.getRol())) {
            FuncionarioDAO funcionarioDAO = new FuncionarioDAOXml();
            FuncionarioService funcionarioService = new FuncionarioService(funcionarioDAO);
            new FuncionarioController(mainWindow.funcionarioPanel, funcionarioService);

            CategoriaRecursoDAO categoriaDAO = new CategoriaRecursoDAOXml();
            CategoriaRecursoService categoriaService = new CategoriaRecursoService(categoriaDAO);
            new CategoriaRecursoController(mainWindow.categoriaPanel, categoriaService);
        }

        mainWindow.setVisible(true);
    }


    private static void sembrarAdministradorInicial(UsuarioDAO usuarioDAO) {
        if (usuarioDAO.listarTodos().isEmpty()) {
            usuarioDAO.guardar(new Administrador(1, "Administrador", "admin", "admin"));
        }
    }
}