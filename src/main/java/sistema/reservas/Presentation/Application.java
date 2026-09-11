package sistema.reservas.Presentation;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Sesion;

import sistema.reservas.Presentation.Actividad.ControllerActividad;
import sistema.reservas.Presentation.Actividad.Services.ServiceActividad;
import sistema.reservas.Presentation.Calendarizacion.ControllerCalendario;
import sistema.reservas.Presentation.Calendarizacion.Services.ServiceCalendario;
import sistema.reservas.Presentation.Categoria.CategoriaModel;
import sistema.reservas.Presentation.Categoria.CategoriaRecursoController;
import sistema.reservas.Presentation.Estadistica.ControllerEstadistica;
import sistema.reservas.Presentation.Estadistica.Service.ServiceEstadistica;
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
            System.out.println("Buscando datos en: " + new java.io.File("data").getAbsolutePath());
        }
    }



    private static void doLogin() {
        LoginView view = new LoginView();
        LoginModel model = new LoginModel();
        new UsuarioController(view, model);

        view.setVisible(true);
    }

    private static void doRun() {
        MainWindow mainWindow = new MainWindow(Sesion.getUsuario());

        // Recurso y Reserva son el nucleo de datos del que tambien
        // dependen Actividades, Calendarizacion y Estadisticas
        // (Integrante 3), asi que se crean una sola vez, sin importar
        // el rol del usuario logueado (esas 3 pestanas se ven siempre).
        RecursoService recursoService = new RecursoService();
        ReservaService reservaService = new ReservaService(recursoService);
        List<CategoriaRecurso> categorias = recursoService.listarCategorias();

        if ("ADMIN".equals(Sesion.getUsuario().getRol())) {
            new FuncionarioController(mainWindow.funcionarioPanel, new FuncionarioModel());
            new CategoriaRecursoController(mainWindow.categoriaPanel, new CategoriaModel());

            for (CategoriaRecurso categoria : categorias) {
                mainWindow.recursoPanel.agregarCategoria(categoria);
            }
            RecursoController recursoController = new RecursoController(recursoService);
            mainWindow.recursoPanel.setController(recursoController);
        }

        if ("FUNCIONARIO".equals(Sesion.getUsuario().getRol())) {
            mainWindow.reservaPanel.setFuncionario((Funcionario) Sesion.getUsuario());

            ReservaController reservaController = new ReservaController(reservaService);
            mainWindow.reservaPanel.setController(reservaController);
        }

        new ControllerActividad(mainWindow.actividadPanel, new ServiceActividad(reservaService));

        new ControllerCalendario(
                mainWindow.calendarioPanel,
                new ServiceCalendario(reservaService, recursoService),
                recursoService::listarCategorias);

        new ControllerEstadistica(mainWindow.estadisticaPanel, new ServiceEstadistica(reservaService));

        mainWindow.setVisible(true);
    }
}