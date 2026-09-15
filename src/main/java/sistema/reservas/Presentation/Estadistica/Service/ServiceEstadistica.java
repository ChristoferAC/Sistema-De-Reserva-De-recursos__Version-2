package sistema.reservas.Presentation.Estadistica.Service;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Presentation.Estadistica.ResultadoEstadistica;
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
 */
public class ServiceEstadistica {

    private final ReservaService reservaService;

    public ServiceEstadistica(ReservaService reservaService) {
        if (reservaService == null) {
            throw new IllegalArgumentException("El ReservaService no puede ser nulo.");
        }
        this.reservaService = reservaService;
    }

    public ResultadoEstadistica contarPorCategoria(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);

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

        return aResultado(conteos);
    }

    public ResultadoEstadistica contarPorSemana(LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);

        Map<String, Integer> conteos = new TreeMap<>();

        for (Reserva reserva : reservaService.listarReservas()) {
            if (!reserva.isActiva() || fueraDeRango(reserva.getFecha(), desde, hasta)) {
                continue;
            }
            LocalDate lunes = reserva.getFecha().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            conteos.merge(lunes.toString(), 1, Integer::sum);
        }

        return aResultado(conteos);
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

    private ResultadoEstadistica aResultado(Map<String, Integer> conteos) {
        List<String> etiquetas = new ArrayList<>(conteos.keySet());
        List<Integer> cantidades = new ArrayList<>();
        for (String etiqueta : etiquetas) {
            cantidades.add(conteos.get(etiqueta));
        }
        return new ResultadoEstadistica(etiquetas, cantidades);
    }
}