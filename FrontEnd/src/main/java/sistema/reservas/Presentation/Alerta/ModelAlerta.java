package sistema.reservas.Presentation.Alerta;

import sistema.reservas.Logic.Reserva;
import sistema.reservas.Presentation.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class ModelAlerta extends AbstractModel {
    List<Reserva> alertas;

    public static final String ALERTAS = "alertas";

    public ModelAlerta() {
        alertas = new ArrayList<Reserva>();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(ALERTAS);
    }

    public List<Reserva> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<Reserva> alertas) {
        this.alertas = alertas;
        firePropertyChange(ALERTAS);
    }
}