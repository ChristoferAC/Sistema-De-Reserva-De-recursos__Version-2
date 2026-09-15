package sistema.reservas.Presentation.Actividad;

import sistema.reservas.Presentation.AbstractModel;

/**
 * Model de MVC para la pantalla de Actividades.
 * Guarda la última matriz cargada y notifica a la View cuando cambia.
 */
public class ModelActividad extends AbstractModel {

    public static final String MATRIZ = "matriz";

    private MatrizActividad matriz;

    public MatrizActividad getMatriz() {
        return matriz;
    }

    public void setMatriz(MatrizActividad matriz) {
        this.matriz = matriz;
        firePropertyChange(MATRIZ);
    }
}