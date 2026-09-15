package sistema.reservas.Presentation.Reserva;

import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Presentation.AbstractModel;

import java.util.ArrayList;
import java.util.List;

public class ReservaModel extends AbstractModel {

    public static final String CURRENT = "current";
    public static final String RESERVAS = "reservas";
    public static final String RECURSOS = "recursos";

    private Reserva current;
    private List<Reserva> reservas;
    private List<Recurso> recursos;

    public ReservaModel() {
        current = null;
        reservas = new ArrayList<>();
        recursos = new ArrayList<>();
    }

    public Reserva getCurrent() {
        return current;
    }

    public void setCurrent(Reserva current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
        firePropertyChange(RESERVAS);
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
        firePropertyChange(RECURSOS);
    }
}