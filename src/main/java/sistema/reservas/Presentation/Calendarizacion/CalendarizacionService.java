package sistema.reservas.Presentation.Calendarizacion;

import sistema.reservas.Presentation.Recurso.RecursoService;
import sistema.reservas.Presentation.Reserva.ReservaService;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de Calendarización de recursos (funcionalidad 6 del enunciado).
 *
 * Dada una fecha y una categoría, arma una matriz de solo lectura:
 * filas = horas del día, columnas = cada recurso de esa categoría, y
 * cada celda indica si el recurso está reservado a esa hora (actividad +
 * funcionario).
 *
 * Importante: esta clase NO tiene persistencia propia. Todos los datos
 * salen de Reserva a través de ReservaService (Integrante 2) y de
 * RecursoService (Integrante 2) para saber qué recursos pertenecen a la
 * categoría consultada.
 */
public class CalendarizacionService {

    // Decisión de diseño: rango de horario laboral que cubre la matriz.
    // El enunciado no especifica el rango exacto; se deja centralizado
    // aquí para poder ajustarlo fácilmente si el equipo decide otro.
    private static final int HORA_INICIO_DIA = 6;
    private static final int HORA_FIN_DIA = 22;

    private final ReservaService reservaService;
    private final RecursoService recursoService;

    public CalendarizacionService(ReservaService reservaService, RecursoService recursoService) {
        this.reservaService = reservaService;
        this.recursoService = recursoService;
    }

    public MatrizCalendarizacion generarMatriz(LocalDate fecha, CategoriaRecurso categoria) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }

        List<Recurso> recursos = recursoService.listarPorCategoria(categoria.getId());
        List<LocalTime> horas = generarHoras();

        List<Reserva> reservasDelDia = new ArrayList<>();
        for (Reserva reserva : reservaService.listarReservas()) {
            if (fecha.equals(reserva.getFecha())) {
                reservasDelDia.add(reserva);
            }
        }

        String[][] celdas = new String[horas.size()][recursos.size()];
        for (int fila = 0; fila < horas.size(); fila++) {
            LocalTime hora = horas.get(fila);
            for (int columna = 0; columna < recursos.size(); columna++) {
                Recurso recurso = recursos.get(columna);
                Reserva ocupante = buscarReservaQueOcupa(reservasDelDia, recurso, hora);
                celdas[fila][columna] = (ocupante == null)
                        ? ""
                        : ocupante.getActividad() + " - " + ocupante.getFuncionario().getNombre();
            }
        }

        return new MatrizCalendarizacion(horas, recursos, celdas);
    }

    private Reserva buscarReservaQueOcupa(List<Reserva> reservasDelDia, Recurso recurso, LocalTime hora) {
        for (Reserva reserva : reservasDelDia) {
            boolean usaEsteRecurso = false;
            for (Recurso asignado : reserva.getRecursosAsignados()) {
                if (asignado.getId().equals(recurso.getId())) {
                    usaEsteRecurso = true;
                    break;
                }
            }
            boolean horaDentroDelRango = !hora.isBefore(reserva.getHoraInicio())
                    && hora.isBefore(reserva.getHoraFin());

            if (usaEsteRecurso && horaDentroDelRango) {
                return reserva;
            }
        }
        return null;
    }

    private List<LocalTime> generarHoras() {
        List<LocalTime> horas = new ArrayList<>();
        for (int h = HORA_INICIO_DIA; h < HORA_FIN_DIA; h++) {
            horas.add(LocalTime.of(h, 0));
        }
        return horas;
    }
}
