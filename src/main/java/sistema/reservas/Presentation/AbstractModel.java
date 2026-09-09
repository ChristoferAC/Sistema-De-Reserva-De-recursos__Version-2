package sistema.reservas.Presentation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Clase base para los Model del patron MVC.
 * Provee el mecanismo de notificacion de cambios (Observer) usando
 * PropertyChangeSupport, cuando una
 * propiedad del Model cambia, se notifica a las Views registradas
 * (que implementan PropertyChangeListener) mediante firePropertyChange.
 */
public abstract class AbstractModel {

    protected PropertyChangeSupport propertyChangeSupport;

    public AbstractModel() {
        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void firePropertyChange(String propertyName) {
        propertyChangeSupport.firePropertyChange(propertyName, null, null);
    }
}