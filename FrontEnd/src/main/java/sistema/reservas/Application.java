// ============================================================
// FRONTEND / src/main/java/sistema/reservas/Application.java
// Migrado al Proxy: ya no se crean RecursoService ni ReservaService; todos los módulos
// usan Service.instance(). Al cerrar la ventana se llama stop() para que el Server
// imprima "Quedan: 0". Si el servidor no está arriba, se avisa en vez de dejar un stack trace.
//
// PENDIENTE DE I1: categorias() devuelve una lista vacía hasta que publique
// findAll(CategoriaRecurso) en IService. Cuando exista, reemplaza el cuerpo por:
//     return Service.instance().findAll(new CategoriaRecurso());
// y el combo de categorías de Recursos y de Calendarización se llenará solo.
// ============================================================
package sistema.reservas;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Service;
import sistema.reservas.Logic.Services.ServiceActividad;
import sistema.reservas.Logic.Services.ServiceCalendario;
import sistema.reservas.Logic.Services.ServiceEstadistica;
import sistema.reservas.Logic.Sesion;
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
import java.util.ArrayList;
import java.util.List;

public class Application {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            // Si el look and feel no esta disponible, se sigue con el por defecto.
        }

        try {
            Service.instance();   // conecta ya: si el servidor no está arriba, falla aquí y no a mitad del login
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Sin conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        doLogin();

        if (Sesion.isLoggedIn()) {
            doRun();
        } else {
            Service.instance().stop();   // cerró el login sin entrar: desconexión limpia
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

        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Service.instance().stop();   // el Server debe imprimir "Quedan: 0"
            }
        });

        if ("ADMIN".equals(Sesion.getUsuario().getRol())) {
            new FuncionarioController(mainWindow.funcionarioPanel, new FuncionarioModel());
            new CategoriaRecursoController(mainWindow.categoriaPanel, new CategoriaModel());
            new RecursoController(mainWindow.recursoPanel, new RecursoModel(), Application::categorias);
        }

        if ("FUNCIONARIO".equals(Sesion.getUsuario().getRol())) {
            mainWindow.reservaPanel.setFuncionario((Funcionario) Sesion.getUsuario());
            new ReservaController(mainWindow.reservaPanel, new ReservaModel());
        }

        new ControllerActividad(mainWindow.actividadPanel, new ServiceActividad());

        new ControllerCalendario(
                mainWindow.calendarioPanel,
                new ServiceCalendario(),
                Application::categoriasSinExcepcion);

        new ControllerEstadistica(mainWindow.estadisticaPanel, new ServiceEstadistica());
        new ControllerAlerta(mainWindow.alertaView, new ModelAlerta());

        mainWindow.setVisible(true);
    }

    // ---------------- categorías (bloque de I1 en IService) ----------------

    private static List<CategoriaRecurso> categorias() throws Exception {
        return new ArrayList<>();   // TODO I1: return Service.instance().findAll(new CategoriaRecurso());
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