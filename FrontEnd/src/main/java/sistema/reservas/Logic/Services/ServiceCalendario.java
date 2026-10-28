// ============================================================
// FRONTEND / src/main/java/sistema/reservas/Logic/Services/ServiceCalendario.java
// Calcula sobre findRecursosByCategoria(...) y findAll(Reserva) del Service (Proxy).
// Se agregaron los imports de Recurso, CategoriaRecurso y Reserva, que antes llegaban
// por estar en el mismo paquete.
// ============================================================
package sistema.reservas.Logic.Services;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.IService;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Presentation.Calendarizacion.ModelCalendario;

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
 * Esta clase NO tiene persistencia propia: los recursos de la categoría salen de
 * findRecursosByCategoria y las reservas de findAll(Reserva), ambos del Service
 * (en el Frontend siempre es el Proxy).
 */
public class ServiceCalendario {

    // Decisión de diseño: rango de horario laboral que cubre la matriz.
    // El enunciado no especifica el rango exacto; se deja centralizado
    // aquí para poder ajustarlo fácilmente si el equipo decide otro.
    private static final int HORA_INICIO_DIA = 6;
    private static final int HORA_FIN_DIA = 22;

    private final IService service;

    public ServiceCalendario() {
        this(Service.instance());
    }

    /** Para pruebas: permite inyectar un IService falso sin abrir un socket. */
    public ServiceCalendario(IService service) {
        if (service == null) {
            throw new IllegalArgumentException("El Service no puede ser nulo.");
        }
        this.service = service;
    }

    public ModelCalendario generarMatriz(LocalDate fecha, CategoriaRecurso categoria) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }

        List<Recurso> recursos;
        List<Reserva> todas;
        try {
            recursos = service.findRecursosByCategoria(categoria);
            todas = service.findAll(new Reserva());
        } catch (Exception ex) {
            // IllegalArgumentException es la que ControllerCalendario ya captura y muestra
            throw new IllegalArgumentException("No se pudo cargar el calendario: " + ex.getMessage(), ex);
        }

        List<LocalTime> horas = generarHoras();

        List<Reserva> reservasDelDia = new ArrayList<>();
        for (Reserva reserva : todas) {
            if (reserva.isActiva() && fecha.equals(reserva.getFecha())) {
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

        return new ModelCalendario(horas, recursos, celdas);
    }

    private Reserva buscarReservaQueOcupa(List<Reserva> reservasDelDia, Recurso recurso, LocalTime hora) {
        for (Reserva reserva : reservasDelDia) {
            boolean usaEsteRecurso = false;
            for (Recurso asignado : reserva.getRecursosAsignados()) {
                if (asignado.getId().equals(recurso.getId())) {   // ids de recurso son texto: equals, nunca ==
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