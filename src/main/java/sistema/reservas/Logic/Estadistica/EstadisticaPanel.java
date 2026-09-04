package sistema.reservas.Logic.Estadistica;

import javax.swing.*;
import java.awt.*;


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
