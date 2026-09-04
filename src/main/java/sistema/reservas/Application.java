package sistema.reservas;

import sistema.reservas.Logic.Categoria.CategoriaRecursoController;
import sistema.reservas.Logic.Funcionario.FuncionarioController;
import sistema.reservas.Logic.Usuario.UsuarioController;
import sistema.reservas.dao.CategoriaRecursoDAO;
import sistema.reservas.dao.CategoriaRecursoDAOXml;
import sistema.reservas.dao.FuncionarioDAO;
import sistema.reservas.dao.FuncionarioDAOXml;
import sistema.reservas.dao.UsuarioDAO;
import sistema.reservas.dao.UsuarioDAOXml;
import sistema.reservas.Logic.Administrador.Administrador;
import sistema.reservas.Logic.Usuario.Usuario;
import sistema.reservas.Logic.Categoria.CategoriaRecursoService;
import sistema.reservas.Logic.Funcionario.FuncionarioService;
import sistema.reservas.Logic.Usuario.UsuarioService;
import sistema.reservas.Presentation.Login.LoginView;
import sistema.reservas.Presentation.Login.MainWindow;

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

        // Funcionarios y Categorias solo existen como pestanas si el
        // usuario es Administrador (ver MainWindow.java), asi que solo
        // tiene sentido conectar sus Controllers en ese caso.
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