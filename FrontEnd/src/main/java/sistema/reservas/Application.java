// ============================================================
// FRONTEND / src/main/java/sistema/reservas/PruebaI2.java
// TEMPORAL, solo para que I2 pruebe Recursos y Reservas sin login (I1 aún no publica el suyo).
// BORRAR cuando I1 publique login(); entonces Application.java vuelve a ser la entrada.
//
// Uso (el Backend debe estar corriendo con el handler de I2 conectado al Worker):
//   java -jar FrontEnd.jar FUNCIONARIO          -> pestaña Reservas, funcionario 1234
//   java -jar FrontEnd.jar FUNCIONARIO 4321     -> igual, pero como el funcionario 4321
//   java -jar FrontEnd.jar ADMIN                -> pestaña Recursos
// El id debe existir en la tabla usuario (semilla de sql/01_usuarios_categorias.sql: 1234, 4321, 2303...).
// Para probar alertas abra DOS Frontends con ids DISTINTOS: el creador de la reserva no recibe su propia alerta.
// ============================================================
package sistema.reservas;

import sistema.reservas.Logic.Administrador;
import sistema.reservas.Logic.AlertaMonitor;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Protocol;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Services.*;
import sistema.reservas.Logic.Sesion;
import sistema.reservas.Presentation.Actividad.ControllerActividad;
import sistema.reservas.Presentation.Alerta.ControllerAlerta;
import sistema.reservas.Presentation.Alerta.ModelAlerta;
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
        int idFuncionario = 1234;
        if (args.length > 1) {
            try {
                idFuncionario = Integer.parseInt(args[1].trim());
            } catch (NumberFormatException ex) {
                System.err.println("Id inválido: '" + args[1] + "'. Uso: Application [FUNCIONARIO|ADMIN] [idFuncionario]");
                return;
            }
        }

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

        Funcionario funcionario = new Funcionario(idFuncionario, "Funcionario de prueba", "funcionario", null, "0000-0000");
        Sesion.setUsuario("ADMIN".equals(rol)
                ? new Administrador(1, "Administrador de prueba", "admin", null)
                : funcionario);

        MainWindow mainWindow = new MainWindow(Sesion.getUsuario());

        // Alertas: el canal asincrónico se abre después del "login", independiente del Service.
        ControllerAlerta controllerAlerta = new ControllerAlerta(mainWindow.alertaView, new ModelAlerta());
        AlertaMonitor alertas = new AlertaMonitor(Protocol.SERVER, Protocol.PORT_ALERTAS,
                Sesion.getUsuario().getId(), controllerAlerta);
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