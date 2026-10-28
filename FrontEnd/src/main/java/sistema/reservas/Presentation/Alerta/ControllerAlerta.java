package sistema.reservas.Presentation.Alerta;

import sistema.reservas.Logic.AlertaListener;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ControllerAlerta implements AlertaListener {
    ViewAlerta view;
    ModelAlerta model;

    public ControllerAlerta(ViewAlerta view, ModelAlerta model) {
        this.view = view;
        this.model = model;
        view.setController(this);
        view.setModel(model);
    }

    // Llega una reserva nueva (en el EDT): se agrega de primera en la lista
    @Override
    public void alerta(Reserva reserva) {
        List<Reserva> alertas = model.getAlertas();
        alertas.addFirst(reserva);
        model.setAlertas(alertas);
    }

    // Solo se quita de la lista local, no se avisa al Backend
    public void descartar(int row) {
        List<Reserva> alertas = model.getAlertas();
        alertas.remove(row);
        model.setAlertas(alertas);
    }

    // Temporal: crea una alerta de prueba mientras no exista el canal del Backend
    int contador = 0;
    public void simular() {
        contador++;
        Funcionario f = new Funcionario(1234, "Juan Mora", "jmora", null, "8888-8888");
        Reserva r = new Reserva(contador, f, "Reunion " + contador, LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(10, 0));
        r.agregarRecurso(new Recurso("1111", "Laptop", "Laptop Dell", null));
        r.agregarRecurso(new Recurso("2221", "Salon Grande", "Salon para eventos", null));
        alerta(r);
    }
}