package sistema.reservas.Presentation.Estadistica;

import sistema.reservas.Presentation.AbstractModel;

/**
 * Model de MVC para la pantalla de Estadísticas.
 * La pantalla tiene 2 bloques independientes (Recursos y Actividades),
 * así que el Model guarda un resultado separado para cada uno y notifica
 * por separado cuál cambió.
 */
public class ModelEstadistica extends AbstractModel {

    public static final String POR_CATEGORIA = "porCategoria"; // bloque Recursos
    public static final String POR_SEMANA = "porSemana";       // bloque Actividades

    private ResultadoEstadistica porCategoria;
    private ResultadoEstadistica porSemana;

    public ResultadoEstadistica getPorCategoria() {
        return porCategoria;
    }

    public void setPorCategoria(ResultadoEstadistica porCategoria) {
        this.porCategoria = porCategoria;
        firePropertyChange(POR_CATEGORIA);
    }

    public ResultadoEstadistica getPorSemana() {
        return porSemana;
    }

    public void setPorSemana(ResultadoEstadistica porSemana) {
        this.porSemana = porSemana;
        firePropertyChange(POR_SEMANA);
    }
}