package sistema.reservas.Presentation.Calendarizacion;

import sistema.reservas.Logic.Recurso;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

/**
 * Resultado de ServiceCalendario.generarMatriz(...).
 * (Antes se llamaba ModelCalendario; se renombró porque ese nombre
 * ahora lo usa el Model real de MVC).
 *
 * Filas = horas del día. Columnas = cada recurso de la categoría
 * consultada. celda[fila][columna] = "" si está libre, o
 * "<actividad> - <funcionario>" si está ocupada en esa hora.
 */
public class MatrizCalendario {

    private final List<LocalTime> horas;
    private final List<Recurso> recursos;
    private final String[][] celdas;

    public MatrizCalendario(List<LocalTime> horas, List<Recurso> recursos, String[][] celdas) {
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