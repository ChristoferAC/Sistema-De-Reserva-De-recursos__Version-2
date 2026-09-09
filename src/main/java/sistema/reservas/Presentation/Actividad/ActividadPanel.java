package sistema.reservas.Presentation.Actividad;

import javax.swing.*;
import java.awt.*;


public class ActividadPanel extends JPanel {

    private JTextField txtFechaReferencia;
    private JButton btnCargar;
    private JButton btnImprimir;
    private JLabel matrizPlaceholder;

    public ActividadPanel() {
        super(new BorderLayout(5, 5));
        construirInterfaz();
    }

    private void construirInterfaz() {
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Fecha de referencia (semana):"));
        txtFechaReferencia = new JTextField(10);
        filtros.add(txtFechaReferencia);

        btnCargar = new JButton("Cargar");
        filtros.add(btnCargar);

        btnImprimir = new JButton("Imprimir");
        filtros.add(btnImprimir);

        matrizPlaceholder = new JLabel(
                "Aquí irá la matriz semanal (filas = hora, columnas = día) — pendiente",
                SwingConstants.CENTER);

        add(filtros, BorderLayout.NORTH);
        add(matrizPlaceholder, BorderLayout.CENTER);
    }

    public JTextField getTxtFechaReferencia() {
        return txtFechaReferencia;
    }

    public JButton getBtnCargar() {
        return btnCargar;
    }

    public JButton getBtnImprimir() {
        return btnImprimir;
    }
}
