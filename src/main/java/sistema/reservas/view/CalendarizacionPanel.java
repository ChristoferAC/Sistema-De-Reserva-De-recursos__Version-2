package sistema.reservas.view;

import javax.swing.*;
import java.awt.*;

public class CalendarizacionPanel extends JPanel {

    public CalendarizacionPanel() {
        super(new BorderLayout());
        JLabel placeholder = new JLabel(
                "Vista de calendarización (disponibilidad y reservas) — pendiente",
                SwingConstants.CENTER);
        add(placeholder, BorderLayout.CENTER);
    }
}
