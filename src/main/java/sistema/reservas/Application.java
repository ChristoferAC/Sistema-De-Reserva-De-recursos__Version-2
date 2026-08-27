package sistema.reservas;

import sistema.reservas.view.MainWindow;

import javax.swing.SwingUtilities;

public class Application {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
