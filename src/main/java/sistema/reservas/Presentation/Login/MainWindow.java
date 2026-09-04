package sistema.reservas.Presentation.Login;

import sistema.reservas.Presentation.Actividad.ActividadPanel;
import sistema.reservas.Presentation.Calendarizacion.CalendarizacionPanel;
import sistema.reservas.Presentation.Categoria.CategoriaPanel;
import sistema.reservas.Presentation.Estadistica.EstadisticaPanel;
import sistema.reservas.Presentation.Funcionario.FuncionarioPanel;
import sistema.reservas.Presentation.Recurso.RecursoPanel;
import sistema.reservas.Presentation.Reserva.ReservaPanel;
import sistema.reservas.Logic.Usuario;

import javax.swing.*;

public class MainWindow extends JFrame {

    private final JTabbedPane tabbedPane;

    public final FuncionarioPanel funcionarioPanel;
    public final CategoriaPanel categoriaPanel;
    public final RecursoPanel recursoPanel;
    public final ReservaPanel reservaPanel;
    public final CalendarizacionPanel calendarizacionPanel;
    public final ActividadPanel actividadPanel;
    public final EstadisticaPanel estadisticaPanel;

    /** Mantiene compatibilidad si algo todavia crea MainWindow sin usuario. */
    public MainWindow() {
        this(null);
    }

    public MainWindow(Usuario usuarioActual) {
        super("Sistema de Reserva de Recursos");

        funcionarioPanel = new FuncionarioPanel();
        categoriaPanel = new CategoriaPanel();
        recursoPanel = new RecursoPanel();
        reservaPanel = new ReservaPanel();
        calendarizacionPanel = new CalendarizacionPanel();
        actividadPanel = new ActividadPanel();
        estadisticaPanel = new EstadisticaPanel();

        boolean esAdministrador = usuarioActual != null && "ADMIN".equals(usuarioActual.getRol());

        tabbedPane = new JTabbedPane();

        // Segun el enunciado: Funcionarios y Categorias solo las puede
        // usar un Administrador. Si no lo es, esas pestanas ni se crean.
        if (esAdministrador) {
            tabbedPane.addTab("Funcionarios", funcionarioPanel);           // I1 - solo Administrador
            tabbedPane.addTab("Categorias", categoriaPanel);               // I1 - solo Administrador
        }

        // TODO (Integrante 2): el enunciado tambien restringe por rol
        // "Lista de recursos" (solo Administrador, funcionalidad 5) y
        // "Reservas" (solo Funcionario, funcionalidad 2). Aplicar el mismo
        // patron aqui cuando se conecte RecursoPanel/ReservaPanel.
        tabbedPane.addTab("Recursos", recursoPanel);                   // I2
        tabbedPane.addTab("Reservas", reservaPanel);                   // I2
        tabbedPane.addTab("Calendarizacion", calendarizacionPanel);    // I3
        tabbedPane.addTab("Actividades", actividadPanel);              // I3
        tabbedPane.addTab("Estadisticas", estadisticaPanel);           // I3

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null);
        add(tabbedPane);
    }
}