package sistema.reservas.Presentation.Estadistica;

import java.util.Collections;
import java.util.List;

/**
 * Resultado de ServiceEstadistica.contarPorCategoria(...) y
 * .contarPorSemana(...).
 * (Antes se llamaba ModelEstadistica; se renombró porque ese nombre
 * ahora lo usa el Model real de MVC).
 */
public class ResultadoEstadistica {

    private final List<String> etiquetas;
    private final List<Integer> cantidades;

    public ResultadoEstadistica(List<String> etiquetas, List<Integer> cantidades) {
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