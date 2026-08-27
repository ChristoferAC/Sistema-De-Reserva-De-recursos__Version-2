package sistema.reservas.view;

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

    public MainWindow() {
        super("Sistema de Reserva de Recursos");

        funcionarioPanel = new FuncionarioPanel();
        categoriaPanel = new CategoriaPanel();
        recursoPanel = new RecursoPanel();
        reservaPanel = new ReservaPanel();
        calendarizacionPanel = new CalendarizacionPanel();
        actividadPanel = new ActividadPanel();
        estadisticaPanel = new EstadisticaPanel();

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Funcionarios", funcionarioPanel);           // I1
        tabbedPane.addTab("Categorias", categoriaPanel);               // I1
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
