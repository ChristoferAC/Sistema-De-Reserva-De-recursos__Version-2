package sistema.reservas.Calendarizacion;

import sistema.reservas.Recurso.Recurso;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de CalendarizacionService.generarMatriz(...).
 *
 * Filas = horas del día. Columnas = cada recurso de la categoría
 * consultada. celda[fila][columna] = "" si está libre, o
 * "<actividad> - <funcionario>" si está ocupada en esa hora.
 *
 * Es un objeto de solo lectura (no se persiste); el Controller lo usa
 * para llenar el JTable de CalendarizacionPanel.
 */
public class MatrizCalendarizacion {

    private final List<LocalTime> horas;
    private final List<Recurso> recursos;
    private final String[][] celdas;

    public MatrizCalendarizacion(List<LocalTime> horas, List<Recurso> recursos, String[][] celdas) {
        this.horas = Collections.unmodifiableList(horas);
        this.recursos = Collections.unmodifiableList(recursos);
        this.celdas = celdas;
    }

    public List<LocalTime> getHoras() {
        return horas;
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public String getCelda(int fila, int columna) {
        return celdas[fila][columna];
    }
}
