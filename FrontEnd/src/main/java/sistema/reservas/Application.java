package sistema.reservas;

import sistema.reservas.Logic.AlertaMonitor;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Protocol;
import sistema.reservas.Logic.Services.Service;
import sistema.reservas.Logic.Services.ServiceActividad;
import sistema.reservas.Logic.Services.ServiceCalendario;
import sistema.reservas.Logic.Services.ServiceEstadistica;
import sistema.reservas.Logic.Sesion;
import sistema.reservas.Logic.Usuario;
import sistema.reservas.Presentation.Actividad.ControllerActividad;
import sistema.reservas.Presentation.Alerta.ControllerAlerta;
import sistema.reservas.Presentation.Alerta.ModelAlerta;
import sistema.reservas.Presentation.Calendarizacion.ControllerCalendario;
import sistema.reservas.Presentation.Categoria.CategoriaModel;
import sistema.reservas.Presentation.Categoria.CategoriaRecursoController;
import sistema.reservas.Presentation.Estadistica.ControllerEstadistica;
import sistema.reservas.Presentation.Funcionario.FuncionarioController;
import sistema.reservas.Presentation.Funcionario.FuncionarioModel;
import sistema.reservas.Presentation.Login.LoginController;
import sistema.reservas.Presentation.Login.LoginModel;
import sistema.reservas.Presentation.Login.LoginView;
import sistema.reservas.Presentation.Login.MainWindow;
import sistema.reservas.Presentation.Recurso.RecursoController;
import sistema.reservas.Presentation.Recurso.RecursoModel;
import sistema.reservas.Presentation.Reserva.ReservaController;
import sistema.reservas.Presentation.Reserva.ReservaModel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class Application {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            // Si el look and feel no esta disponible, se sigue con el por defecto.
        }

        try {
            Service.instance();   // conecta ya: si el servidor no esta arriba, falla aqui y no a mitad del login
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Sin conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        doLogin();

        if (Sesion.isLoggedIn()) {
            doRun();
        } else {
            Service.instance().stop();   // cerro el login sin entrar: desconexion limpia
        }
    }

    private static void doLogin() {
        LoginView view = new LoginView();
        LoginModel model = new LoginModel();
        new LoginController(view, model);

        view.setVisible(true);   // JDialog modal: se queda aqui hasta que entra o cierra
    }

    private static void doRun() {
        Usuario usuario = Sesion.getUsuario();
        MainWindow mainWindow = new MainWindow(usuario);

        // Alertas: el canal asincronico se abre despues del login, aparte del Service
        ControllerAlerta controllerAlerta = new ControllerAlerta(mainWindow.alertaView, new ModelAlerta());
        AlertaMonitor alertas = new AlertaMonitor(Protocol.SERVER, Protocol.PORT_ALERTAS,
                usuario.getId(), controllerAlerta);
        try {
            alertas.start();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo abrir el canal de alertas: " + ex.getMessage()
                            + "\nLa aplicación funcionará, pero no recibirá alertas de otros usuarios.",
                    "Alertas", JOptionPane.WARNING_MESSAGE);
        }

        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                alertas.close();
                Service.instance().stop();   // el Server imprime "Quedan: 0"
            }
        });

        if ("ADMIN".equals(usuario.getRol())) {
            new FuncionarioController(mainWindow.funcionarioPanel, new FuncionarioModel());
            new CategoriaRecursoController(mainWindow.categoriaPanel, new CategoriaModel());
            new RecursoController(mainWindow.recursoPanel, new RecursoModel(), Application::categorias);
        }

        if ("FUNCIONARIO".equals(usuario.getRol())) {
            mainWindow.reservaPanel.setFuncionario((Funcionario) usuario);
            new ReservaController(mainWindow.reservaPanel, new ReservaModel());
        }

        // Calendarizacion, Actividades y Estadisticas se ven con cualquier rol
        new ControllerActividad(mainWindow.actividadPanel, new ServiceActividad());
        new ControllerCalendario(mainWindow.calendarioPanel, new ServiceCalendario(),
                Application::categoriasSinExcepcion);
        new ControllerEstadistica(mainWindow.estadisticaPanel, new ServiceEstadistica());

        mainWindow.setVisible(true);
    }

    /** Las categorias vienen del Backend (tabla categoria). */
    private static List<CategoriaRecurso> categorias() throws Exception {
        return Service.instance().findAll(new CategoriaRecurso());
    }

    /** ControllerCalendario pide un Supplier, que no puede lanzar excepciones. */
    private static List<CategoriaRecurso> categoriasSinExcepcion() {
        try {
            return categorias();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron cargar las categorías: " + ex.getMessage(), ex);
        }
    }
}