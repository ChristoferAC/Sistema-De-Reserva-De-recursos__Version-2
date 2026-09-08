package sistema.reservas.Presentation.Actividad;

import sistema.reservas.Presentation.Actividad.Services.ServiceActividad;
import sistema.reservas.Presentation.Reserva.ReservaService;

import javax.swing.*;

/**
 * Clase de prueba (NO es un JUnit test, ni va en el entregable final).
 * Solo sirve para verificar que ActividadService, ActividadController,
 * ModelActividad y ViewActividad compilan y se conectan bien entre sí,
 * sin depender de Application.java ni de los módulos rotos de
 * Usuario/Funcionario/Categoría.
 *
 * Usa ReservaService() real (lee/crea data/reservas.xml tal cual lo
 * haría la app completa). Si no hay reservas guardadas todavía, la
 * tabla va a salir vacía al presionar "Cargar" — eso es esperado,
 * significa que compiló y corrió sin errores.
 */
public class Application {

    public static void main(String[] args) {
        ReservaService reservaService = new ReservaService();
        ServiceActividad service = new ServiceActividad(reservaService);

        ViewActividad view = new ViewActividad();
        new ControllerActividad(view, service);

        JFrame ventana = new JFrame("Prueba - Actividades");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setContentPane(view.getPanel1());
        ventana.setSize(700, 500);
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        System.out.println("Compiló y arrancó sin errores.");
    }
}