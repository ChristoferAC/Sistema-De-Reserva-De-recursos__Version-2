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
/*package sistema.reservas;

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


    private static List<CategoriaRecurso> categoriasSinExcepcion() {
        try {
            return categorias();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron cargar las categorías: " + ex.getMessage(), ex);
        }
    }
}*/


// ============================================================
// FRONTEND / src/main/java/sistema/reservas/PruebaI2.java
// TEMPORAL, solo para que I2 pruebe Recursos y Reservas sin login (I1 aún no publica el suyo).
// BORRAR cuando I1 publique login(); entonces Application.java vuelve a ser la entrada.
//
// Uso (el Backend debe estar corriendo con el handler de I2 conectado al Worker):
//   java -cp FrontEnd.jar sistema.reservas.PruebaI2 FUNCIONARIO   -> pestaña Reservas
//   java -cp FrontEnd.jar sistema.reservas.PruebaI2 ADMIN          -> pestaña Recursos
// El funcionario usa el id 1234, el de la semilla de sql/02_recursos_reservas.sql
// (debe existir en la tabla usuario, creada por el script de I1).
// ============================================================
package sistema.reservas;

import sistema.reservas.Logic.Administrador;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Service;
import sistema.reservas.Logic.Services.ServiceActividad;
import sistema.reservas.Logic.Services.ServiceCalendario;
import sistema.reservas.Logic.Services.ServiceEstadistica;
import sistema.reservas.Logic.Sesion;
import sistema.reservas.Presentation.Actividad.ControllerActividad;
import sistema.reservas.Presentation.Calendarizacion.ControllerCalendario;
import sistema.reservas.Presentation.Estadistica.ControllerEstadistica;
import sistema.reservas.Presentation.Login.MainWindow;
import sistema.reservas.Presentation.Recurso.RecursoController;
import sistema.reservas.Presentation.Recurso.RecursoModel;
import sistema.reservas.Presentation.Reserva.ReservaController;
import sistema.reservas.Presentation.Reserva.ReservaModel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Application { //Prueba

    public static void main(String[] args) {
        String rol = args.length > 0 ? args[0].trim().toUpperCase() : "FUNCIONARIO";

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            // se sigue con el look and feel por defecto
        }

        try {
            Service.instance();   // conecta; si el servidor no está arriba falla aquí con mensaje claro
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Sin conexión", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Funcionario funcionario = new Funcionario(1234, "Funcionario de prueba", "funcionario", null, "0000-0000");
        Sesion.setUsuario("ADMIN".equals(rol)
                ? new Administrador(1, "Administrador de prueba", "admin", null)
                : funcionario);

        MainWindow mainWindow = new MainWindow(Sesion.getUsuario());
        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Service.instance().stop();   // el Server debe imprimir "Quedan: 0"
            }
        });

        if ("ADMIN".equals(rol)) {
            new RecursoController(mainWindow.recursoPanel, new RecursoModel(), Application::categoriasDeLosRecursos);
        } else {
            mainWindow.reservaPanel.setFuncionario(funcionario);
            new ReservaController(mainWindow.reservaPanel, new ReservaModel());
        }

        // Módulos de I3, que dependen de findAll(Reserva) y findRecursosByCategoria
        new ControllerActividad(mainWindow.actividadPanel, new ServiceActividad());
        new ControllerCalendario(mainWindow.calendarioPanel, new ServiceCalendario(),
                Application::categoriasDeLosRecursosSinExcepcion);
        new ControllerEstadistica(mainWindow.estadisticaPanel, new ServiceEstadistica());

        mainWindow.setVisible(true);
    }

    /**
     * Categorías distintas de los recursos que ya existen (sin duplicados, en orden). Sustituye
     * al findAll(CategoriaRecurso) de I1; con la semilla salen las categorías 1, 2 y 3.
     */
    private static List<CategoriaRecurso> categoriasDeLosRecursos() throws Exception {
        Map<Integer, CategoriaRecurso> distintas = new LinkedHashMap<>();
        for (Recurso recurso : Service.instance().findAll(new Recurso())) {
            if (recurso.getCategoria() != null) {
                distintas.putIfAbsent(recurso.getCategoria().getId(), recurso.getCategoria());
            }
        }
        return new ArrayList<>(distintas.values());
    }

    /** ControllerCalendario pide un Supplier, que no puede lanzar excepciones. */
    private static List<CategoriaRecurso> categoriasDeLosRecursosSinExcepcion() {
        try {
            return categoriasDeLosRecursos();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron cargar las categorías: " + ex.getMessage(), ex);
        }
    }
}