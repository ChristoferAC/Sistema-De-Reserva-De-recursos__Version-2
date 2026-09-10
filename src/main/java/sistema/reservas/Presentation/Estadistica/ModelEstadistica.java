package sistema.reservas.Presentation.Estadistica;

import java.util.Collections;
import java.util.List;

/**
 * Resultado de ServiceEstadistica.contarPorCategoria(...) y
 * .contarPorSemana(...).
 *
 * Es genérico a propósito: los dos bloques de la pantalla (Recursos y
 * Actividades) necesitan exactamente lo mismo — una lista de
 * etiquetas (nombre de categoría, o fecha de inicio de semana) y una
 * cantidad por cada una — así que no hace falta una clase distinta
 * para cada bloque.
 */
public class ModelEstadistica {

    private final List<String> etiquetas;
    private final List<Integer> cantidades;

    public ModelEstadistica(List<String> etiquetas, List<Integer> cantidades) {
        this.etiquetas = Collections.unmodifiableList(etiquetas);
        this.cantidades = Collections.unmodifiableList(cantidades);
    }

    public List<String> getEtiquetas() {
        return etiquetas;
    }

    public List<Integer> getCantidades() {
        return cantidades;
    }
}