package sistema.reservas.Presentation.Actividad;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de ServiceActividad.generarMatriz(...).
 * (Antes se llamaba ModelActividad; se renombró porque ese nombre
 * ahora lo usa el Model real de MVC).
 */
public class MatrizActividad {

    private final List<LocalTime> horas;
    private final List<LocalDate> dias;
    private final String[][] celdas;

    public MatrizActividad(List<LocalTime> horas, List<LocalDate> dias, String[][] celdas) {
        this.horas = Collections.unmodifiableList(horas);
        this.dias = Collections.unmodifiableList(dias);
        this.celdas = celdas;
    }

    public List<LocalTime> getHoras() { return horas; }
    public List<LocalDate> getDias() { return dias; }
    public String getCelda(int fila, int columna) { return celdas[fila][columna]; }
}