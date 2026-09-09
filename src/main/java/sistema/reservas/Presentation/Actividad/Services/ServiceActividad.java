package sistema.reservas.Presentation.Actividad.Services;

import sistema.reservas.Presentation.Actividad.ModelActividad;
import sistema.reservas.Presentation.Reserva.ReservaService;
import sistema.reservas.Logic.Reserva;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de Actividades (funcionalidad 7 del enunciado).
 *
 * Dada una fecha de referencia, arma una matriz de solo lectura:
 * filas = horas del día, columnas = los 7 días de esa semana (lunes a
 * domingo), y cada celda muestra la(s) actividad(es) reservada(s) en
 * ese día y hora.
 *
 * Igual que CalendarizacionService, esta clase NO tiene persistencia
 * propia: todos los datos salen de Reserva a través de ReservaService
 * (Integrante 2).
 */
public class ServiceActividad {

    // Decisión de diseño: mismo rango horario que Calendarización
    // (6:00 a 22:00), para mantener consistencia entre ambos módulos.
    // El enunciado no especifica el rango exacto.
    private static final int HORA_INICIO_DIA = 6;
    private static final int HORA_FIN_DIA = 22;

    private final ReservaService reservaService;

    public ServiceActividad(ReservaService reservaService) {
        if (reservaService == null) {
            throw new IllegalArgumentException("El ReservaService no puede ser nulo.");
        }
        this.reservaService = reservaService;
    }

    public ModelActividad generarMatriz(LocalDate fechaReferencia) {
        if (fechaReferencia == null) {
            throw new IllegalArgumentException("La fecha de referencia es obligatoria.");
        }

        List<LocalDate> dias = generarDiasDeLaSemana(fechaReferencia);
        List<LocalTime> horas = generarHoras();

        List<Reserva> reservasDeLaSemana = new ArrayList<>();
        for (Reserva reserva : reservaService.listarReservas()) {
            // Decisión de diseño: solo se muestran reservas ACTIVAS;
            // una reserva cancelada no debe aparecer como actividad vigente.
            if (reserva.isActiva() && dias.contains(reserva.getFecha())) {
                reservasDeLaSemana.add(reserva);
            }
        }

        String[][] celdas = new String[horas.size()][dias.size()];
        for (int fila = 0; fila < horas.size(); fila++) {
            LocalTime hora = horas.get(fila);
            for (int columna = 0; columna < dias.size(); columna++) {
                LocalDate dia = dias.get(columna);
                celdas[fila][columna] = actividadesEn(reservasDeLaSemana, dia, hora);
            }
        }

        return new ModelActividad(horas, dias, celdas);
    }

    /**
     * Junta el texto de todas las reservas que ocupan ese día y hora.
     * A diferencia de Calendarización (una columna = un recurso), aquí
     * una columna es un día completo, así que puede haber más de una
     * actividad al mismo tiempo si usan recursos distintos.
     */
    private String actividadesEn(List<Reserva> reservas, LocalDate dia, LocalTime hora) {
        StringBuilder texto = new StringBuilder();

        for (Reserva reserva : reservas) {
            boolean mismoDia = dia.equals(reserva.getFecha());
            boolean horaDentroDelRango = !hora.isBefore(reserva.getHoraInicio())
                    && hora.isBefore(reserva.getHoraFin());

            if (mismoDia && horaDentroDelRango) {
                if (texto.length() > 0) {
                    texto.append("; ");
                }
                texto.append(reserva.getActividad())
                        .append(" - ")
                        .append(reserva.getFuncionario().getNombre());
            }
        }

        return texto.toString();
    }

    private List<LocalDate> generarDiasDeLaSemana(LocalDate fechaReferencia) {
        LocalDate lunes = fechaReferencia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<LocalDate> dias = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            dias.add(lunes.plusDays(i));
        }
        return dias;
    }

    private List<LocalTime> generarHoras() {
        List<LocalTime> horas = new ArrayList<>();
        for (int h = HORA_INICIO_DIA; h < HORA_FIN_DIA; h++) {
            horas.add(LocalTime.of(h, 0));
        }
        return horas;
    }
}