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