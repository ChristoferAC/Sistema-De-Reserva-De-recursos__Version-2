package sistema.reservas.Presentation.Estadistica.Services;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Presentation.Estadistica.ModelEstadistica;
import sistema.reservas.Presentation.Reserva.ReservaService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Servicio de Estadísticas (funcionalidad 8 del enunciado).
 *
 * Igual que Calendarización y Actividades, no tiene persistencia
 * propia: todos los conteos salen de Reserva a través de
 * ReservaService (Integrante 2).
 */
public class ServiceEstadistica {

    private final ReservaService reservaService;

    public ServiceEstadistica(ReservaService reservaService) {
        if (reservaService == null) {
            throw new IllegalArgumentException("El ReservaService no puede ser nulo.");
        }
        this.reservaService = reservaService;
    }

    /**
     * Cuenta cuántas reservas activas, dentro del rango [desde, hasta],
     * solicitaron cada categoría de recurso. Si una reserva pidió
     * varias categorías, cuenta una vez por cada una.
     */
    public ModelEstadistica contarPorCategoria(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);

        // Decisión de diseño: LinkedHashMap para conservar el orden en
        // que aparece cada categoría por primera vez (no hay un orden
        // "correcto" evidente para categorías, a diferencia de las
        // semanas que sí tiene sentido ordenar cronológicamente).
        Map<String, Integer> conteos = new LinkedHashMap<>();

        for (Reserva reserva : reservaService.listarReservas()) {
            if (!reserva.isActiva() || fueraDeRango(reserva.getFecha(), desde, hasta)) {
                continue;
            }
            for (CategoriaRecurso categoria : reserva.getCategoriasSolicitadas()) {
                String nombre = nombreCategoria(categoria);
                conteos.merge(nombre, 1, Integer::sum);
            }
        }

        return aModelo(conteos);
    }

    /**
     * Cuenta cuántas reservas activas hubo en cada semana (identificada
     * por la fecha del lunes de esa semana) dentro del rango
     * [desde, hasta].
     */
    public ModelEstadistica contarPorSemana(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);

        // TreeMap: las claves son fechas en formato AAAA-MM-DD, así que
        // ordenan cronológicamente solas.
        Map<String, Integer> conteos = new TreeMap<>();

        for (Reserva reserva : reservaService.listarReservas()) {
            if (!reserva.isActiva() || fueraDeRango(reserva.getFecha(), desde, hasta)) {
                continue;
            }
            LocalDate lunes = reserva.getFecha().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            conteos.merge(lunes.toString(), 1, Integer::sum);
        }

        return aModelo(conteos);
    }

    private void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar ambas fechas.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'.");
        }
    }

    private boolean fueraDeRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return fecha.isBefore(desde) || fecha.isAfter(hasta);
    }

    private String nombreCategoria(CategoriaRecurso categoria) {
        if (categoria.getNombre() != null && !categoria.getNombre().isBlank()) {
            return categoria.getNombre();
        }
        return categoria.getDescripcion();
    }

    private ModelEstadistica aModelo(Map<String, Integer> conteos) {
        List<String> etiquetas = new ArrayList<>(conteos.keySet());
        List<Integer> cantidades = new ArrayList<>();
        for (String etiqueta : etiquetas) {
            cantidades.add(conteos.get(etiqueta));
        }
        return new ModelEstadistica(etiquetas, cantidades);
    }
}