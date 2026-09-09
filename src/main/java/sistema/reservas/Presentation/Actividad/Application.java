package sistema.reservas.Presentation.Actividad;

import sistema.reservas.Logic.Sesion;
import sistema.reservas.Presentation.Categoria.CategoriaRecursoController;
import sistema.reservas.Presentation.Categoria.CategoriaModel;
import sistema.reservas.Presentation.Funcionario.FuncionarioController;
import sistema.reservas.Presentation.Funcionario.FuncionarioModel;
import sistema.reservas.Presentation.Login.LoginModel;
import sistema.reservas.Presentation.Login.LoginView;
import sistema.reservas.Presentation.Login.MainWindow;
import sistema.reservas.Presentation.Login.UsuarioController;

import javax.swing.*;

public class Application {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            // Si el look and feel no esta disponible, se sigue con el por defecto.
        }

        doLogin();

        if (Sesion.isLoggedIn()) {
            doRun();
        }
    }

    private static void doLogin() {
        LoginView view = new LoginView();
        LoginModel model = new LoginModel();
        new UsuarioController(view, model);

        // Como LoginView es un JDialog modal, esta linea bloquea la
        // aplicacion hasta que el usuario haga login exitoso (dispose())
        // o cierre la ventana (lo cual termina el programa, ver
        // setDefaultCloseOperation en LoginView).
        view.setVisible(true);
    }

    private static void doRun() {
        MainWindow mainWindow = new MainWindow(Sesion.getUsuario());

        // Funcionarios y Categorias solo existen como pestanas si el
        // usuario es Administrador (ver MainWindow.java), asi que solo
        // tiene sentido conectar sus Controllers en ese caso.
        if ("ADMIN".equals(Sesion.getUsuario().getRol())) {
            new FuncionarioController(mainWindow.funcionarioPanel, new FuncionarioModel());
            new CategoriaRecursoController(mainWindow.categoriaPanel, new CategoriaModel());
        }

        mainWindow.setVisible(true);
    }
}