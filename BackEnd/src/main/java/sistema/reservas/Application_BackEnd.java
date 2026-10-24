package sistema.reservas;


import javax.swing.*;
import java.util.List;

public class Application_BackEnd {
/*
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
        new LoginController(view, model);

        view.setVisible(true);
    }

    private static void doRun() {
        MainWindow mainWindow = new MainWindow(Sesion.getUsuario());

        // Recurso y Reserva son el nucleo de datos del que tambien
        // dependen Actividades, Calendarizacion y Estadisticas
        // (Integrante 3), asi que se crean una sola vez, sin importar
        // el rol del usuario logueado (esas 3 pestanas se ven siempre).
        new RecursoController(mainWindow.recursoPanel, new RecursoModel());
        new ReservaController(mainWindow.reservaPanel, new ReservaModel());
        List<CategoriaRecurso> categorias = recursoService.listarCategorias();

        if ("ADMIN".equals(Sesion.getUsuario().getRol())) {
            new FuncionarioController(mainWindow.funcionarioPanel, new FuncionarioModel());
            new CategoriaRecursoController(mainWindow.categoriaPanel, new CategoriaModel());
            new RecursoController(mainWindow.recursoPanel, new RecursoModel(), recursoService);
        }

        if ("FUNCIONARIO".equals(Sesion.getUsuario().getRol())) {
            mainWindow.reservaPanel.setFuncionario((Funcionario) Sesion.getUsuario());
            new ReservaController(mainWindow.reservaPanel, new ReservaModel(), reservaService);
        }

        new ControllerActividad(mainWindow.actividadPanel, new ServiceActividad(reservaService));

        new ControllerCalendario(
                mainWindow.calendarioPanel,
                new ServiceCalendario(reservaService, recursoService),
                recursoService::listarCategorias);

        new ControllerEstadistica(mainWindow.estadisticaPanel, new ServiceEstadistica(reservaService));
        new ControllerAlerta(mainWindow.alertaView, new ModelAlerta());

        mainWindow.setVisible(true);
    }*/

}