package sistema.reservas.Presentation;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Sesion;
import sistema.reservas.Presentation.Categoria.CategoriaRecursoController;
import sistema.reservas.Presentation.Categoria.CategoriaModel;
import sistema.reservas.Presentation.Funcionario.FuncionarioController;
import sistema.reservas.Presentation.Funcionario.FuncionarioModel;
import sistema.reservas.Presentation.Login.LoginModel;
import sistema.reservas.Presentation.Login.LoginView;
import sistema.reservas.Presentation.Login.MainWindow;
import sistema.reservas.Presentation.Recurso.RecursoController;
import sistema.reservas.Presentation.Recurso.RecursoService;
import sistema.reservas.Presentation.Reserva.ReservaController;
import sistema.reservas.Presentation.Reserva.ReservaService;
import sistema.reservas.Presentation.Usuario.UsuarioController;

import javax.swing.*;
import java.util.List;

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

        // Recurso/Reserva comparten el catalogo de categorias, asi que el
        // RecursoService (dueno de listarCategorias()) se crea una sola
        // vez sin importar el rol del usuario logueado.
        RecursoService recursoService = new RecursoService();
        List<CategoriaRecurso> categorias = recursoService.listarCategorias();

        // Funcionarios y Categorias solo existen como pestanas si el
        // usuario es Administrador (ver MainWindow.java), asi que solo
        // tiene sentido conectar sus Controllers en ese caso.
        if ("ADMIN".equals(Sesion.getUsuario().getRol())) {
            new FuncionarioController(mainWindow.funcionarioPanel, new FuncionarioModel());
            new CategoriaRecursoController(mainWindow.categoriaPanel, new CategoriaModel());

            // Recursos (funcionalidad 5) solo la usa un Administrador.
            for (CategoriaRecurso categoria : categorias) {
                mainWindow.recursoPanel.agregarCategoria(categoria);
            }
            RecursoController recursoController = new RecursoController(recursoService);
            mainWindow.recursoPanel.setController(recursoController);
        }

        // Reservas (funcionalidad 2) solo la usa un Funcionario.
        if ("FUNCIONARIO".equals(Sesion.getUsuario().getRol())) {
            mainWindow.reservaPanel.setFuncionario((Funcionario) Sesion.getUsuario());

            ReservaService reservaService = new ReservaService(recursoService);
            ReservaController reservaController = new ReservaController(reservaService);
            mainWindow.reservaPanel.setController(reservaController);
        }

        mainWindow.setVisible(true);
    }
}