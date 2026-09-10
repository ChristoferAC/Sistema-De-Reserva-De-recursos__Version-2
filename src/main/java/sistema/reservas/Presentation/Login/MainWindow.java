package sistema.reservas.Presentation.Login;

import sistema.reservas.Presentation.Actividad.ViewActividad;
import sistema.reservas.Presentation.Calendarizacion.ViewCalendario;
import sistema.reservas.Presentation.Estadistica.ViewEstadistica;
import sistema.reservas.Presentation.Categoria.CategoriaPanel;
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
    public final ViewCalendario calendarioPanel;
    public final ViewActividad actividadPanel;
    public final ViewEstadistica estadisticaPanel;

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
        calendarioPanel = new ViewCalendario();
        actividadPanel = new ViewActividad();
        estadisticaPanel = new ViewEstadistica();

        boolean esAdministrador = usuarioActual != null && "ADMIN".equals(usuarioActual.getRol());
        boolean esFuncionario = usuarioActual != null && "FUNCIONARIO".equals(usuarioActual.getRol());

        tabbedPane = new JTabbedPane();

        // Segun el enunciado: Funcionarios y Categorias solo las puede
        // usar un Administrador. Si no lo es, esas pestanas ni se crean.
        if (esAdministrador) {
            tabbedPane.addTab("Funcionarios", funcionarioPanel.getPanel1()); // I1 - solo Administrador
            tabbedPane.addTab("Categorias", categoriaPanel.getPanel1());   // I1 - solo Administrador
        }

        if (esAdministrador) {          // I2 - solo Administrador.
            tabbedPane.addTab("Recursos", recursoPanel.getPanel());
        }
        if (esFuncionario) {            //I2 - solo Funcionario
            tabbedPane.addTab("Reservas", reservaPanel.getPanel());
        }

        // ViewActividad/ViewCalendario exponen getPanel1() (no getPanel()),
        // que es como IntelliJ nombro el panel raiz del formulario.
        tabbedPane.addTab("Calendarizacion", calendarioPanel.getPanel1());     // I3
        tabbedPane.addTab("Actividades", actividadPanel.getPanel1());              // I3
        tabbedPane.addTab("Estadisticas", estadisticaPanel.getEstadistica());           // I3

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null);
        add(tabbedPane);
    }
}