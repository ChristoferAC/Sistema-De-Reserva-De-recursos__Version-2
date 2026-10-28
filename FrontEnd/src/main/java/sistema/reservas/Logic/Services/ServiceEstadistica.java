// ============================================================
// FRONTEND / src/main/java/sistema/reservas/Logic/Services/ServiceEstadistica.java
// Calcula sobre findAll(Reserva) del Service (Proxy). La lógica de conteo no cambia.
// ============================================================
package sistema.reservas.Logic.Services;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.IService;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Presentation.Estadistica.ModelEstadistica;

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
 * propia: todos los conteos salen de findAll(Reserva) del Service.
 */
public class ServiceEstadistica {

    private final IService service;

    public ServiceEstadistica() {
        this(Service.instance());
    }

    /** Para pruebas: permite inyectar un IService falso sin abrir un socket. */
    public ServiceEstadistica(IService service) {
        if (service == null) {
            throw new IllegalArgumentException("El Service no puede ser nulo.");
        }
        this.service = service;
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

        for (Reserva reserva : cargarReservas()) {
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

        for (Reserva reserva : cargarReservas()) {
            if (!reserva.isActiva() || fueraDeRango(reserva.getFecha(), desde, hasta)) {
                continue;
            }
            LocalDate lunes = reserva.getFecha().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            conteos.merge(lunes.toString(), 1, Integer::sum);
        }

        return aModelo(conteos);
    }

    /**
     * findAll declara "throws Exception" (red o error del Backend). Se convierte en
     * IllegalArgumentException porque es la que ControllerEstadistica ya captura
     * y muestra con el mensaje real.
     */
    private List<Reserva> cargarReservas() {
        try {
            return service.findAll(new Reserva());
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se pudieron cargar las reservas: " + ex.getMessage(), ex);
        }
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