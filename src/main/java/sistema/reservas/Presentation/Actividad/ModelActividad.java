package sistema.reservas.Presentation.Actividad;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;


/**
 * Resultado de ActividadService.generarMatriz(...).
 *
 * Filas = horas del día. Columnas = cada uno de los 7 días de la semana
 * (lunes a domingo) que contiene la fecha de referencia consultada.
 * celda[fila][columna] = "" si no hay actividad en esa hora/día, o el
 * texto de la(s) actividad(es) que ocurren ahí.
 *
 * Es un objeto de solo lectura (no se persiste); el Controller lo usa
 * para llenar el JTable de ViewActividad. Mismo patrón que
 * MatrizCalendarizacion (en el módulo de Calendarización), adaptado a
 * columnas por día en vez de por recurso.
 */
public class ModelActividad {

    private final List<LocalTime> horas;
    private final List<LocalDate> dias;
    private final String[][] celdas;

    public ModelActividad(List<LocalTime> horas, List<LocalDate> dias, String[][] celdas) {
        this.horas = Collections.unmodifiableList(horas);
        this.dias = Collections.unmodifiableList(dias);
        this.celdas = celdas;
    }

    public List<LocalTime> getHoras() {
        return horas;
    }

    public List<LocalDate> getDias() {
        return dias;
    }

    public String getCelda(int fila, int columna) {
        return celdas[fila][columna];
    }
}