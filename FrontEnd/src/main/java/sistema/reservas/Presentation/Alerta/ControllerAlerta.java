package sistema.reservas.Presentation.Alerta;

import sistema.reservas.Logic.AlertaListener;
import sistema.reservas.Logic.Reserva;

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

}