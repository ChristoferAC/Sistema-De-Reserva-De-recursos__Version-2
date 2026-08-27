package sistema.reservas.view;

import javax.swing.*;
import java.awt.*;

/**
 * Pestaña de Estadísticas. Responsable: Integrante 3.
 *
 * Estructura prevista (a implementar en tu etapa):
 *  - Filtros: fecha desde / fecha hasta / tipo (recursos o actividades).
 *  - Área central: gráfico de barras (cantidad por categoría o por semana).
 * Por ahora solo se deja el esqueleto visual con placeholders.
 */
public class EstadisticaPanel extends JPanel {

    private JTextField txtFechaDesde;
    private JTextField txtFechaHasta;
    private JButton btnGenerar;

    public EstadisticaPanel() {
        super(new BorderLayout(5, 5));
        construirInterfaz();
    }

    private void construirInterfaz() {
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Desde:"));
        txtFechaDesde = new JTextField(8);
        filtros.add(txtFechaDesde);
        filtros.add(new JLabel("Hasta:"));
        txtFechaHasta = new JTextField(8);
        filtros.add(txtFechaHasta);
        btnGenerar = new JButton("Generar gráfico");
        filtros.add(btnGenerar);

        JLabel graficoPlaceholder = new JLabel(
                "Aquí se mostrará el gráfico de barras (recursos/actividades)",
                SwingConstants.CENTER);

        add(filtros, BorderLayout.NORTH);
        add(graficoPlaceholder, BorderLayout.CENTER);
    }

    public JTextField getTxtFechaDesde() {
        return txtFechaDesde;
    }

    public JTextField getTxtFechaHasta() {
        return txtFechaHasta;
    }

    public JButton getBtnGenerar() {
        return btnGenerar;
    }
}
