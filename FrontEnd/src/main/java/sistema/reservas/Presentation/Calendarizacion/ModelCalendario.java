package sistema.reservas.Presentation.Calendarizacion;

import sistema.reservas.Presentation.AbstractModel;

/**
 * Model de MVC para la pantalla de Calendarización.
 * Guarda la última matriz cargada y notifica a la View cuando cambia.
 */
public class ModelCalendario extends AbstractModel {

    public static final String MATRIZ = "matriz";

    private MatrizCalendario matriz;

    public MatrizCalendario getMatriz() {
        return matriz;
    }

    public void setMatriz(MatrizCalendario matriz) {
        this.matriz = matriz;
        firePropertyChange(MATRIZ);
    }
}