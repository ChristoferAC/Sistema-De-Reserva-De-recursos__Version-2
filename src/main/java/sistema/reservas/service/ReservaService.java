package sistema.reservas.service;
import sistema.reservas.dao.ReservaDAO;
import sistema.reservas.dao.RecursoDAO;
import sistema.reservas.model.Reserva;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservaService {
    private final ReservaDAO reservaDAO;
    private final RecursoDAO recursoDAO;

    public ReservaService(ReservaDAO reservaDAO, RecursoDAO recursoDAO) {
        this.reservaDAO = reservaDAO;
        this.recursoDAO = recursoDAO;
    }

    public void crearReserva(Reserva reserva) {
        validarReserva(reserva);
        if (!hayDisponibilidad(reserva)) {
            throw new IllegalArgumentException("No hay disponibilidad para todos los recursos solicitados.");
        }
        asignarRecursos(reserva);
        reservaDAO.guardar(reserva);
    }

    public Reserva buscarReserva(int id) {
        return reservaDAO.buscarPorId(id);
    }

    public List<Reserva> listarReservas() {
        return reservaDAO.listar();
    }

    public List<Reserva> listarReservasFuncionario(int idFuncionario) {
        return reservaDAO.listarPorFuncionario(idFuncionario);
    }

    public void modificarReserva(Reserva reserva) {
        validarReserva(reserva);
        if (reservaDAO.buscarPorId(reserva.getId()) == null) {
            throw new IllegalArgumentException("La reserva no existe.");
        }
        reservaDAO.actualizar(reserva);
    }

    public void cancelarReserva(int id) {
        Reserva reserva = reservaDAO.buscarPorId(id);
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no existe.");
        }
        liberarRecursos(reserva);
        reservaDAO.eliminar(id);
    }

    private void validarReserva(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }
        if (reserva.getFuncionario() == null) {
            throw new IllegalArgumentException("La reserva debe tener un funcionario.");
        }
        if (reserva.getActividad() == null || reserva.getActividad().trim().isEmpty()) {
            throw new IllegalArgumentException("La actividad es obligatoria.");
        }

        if (reserva.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (reserva.getHoraInicio() == null || reserva.getHoraFin() == null) {
            throw new IllegalArgumentException("Las horas de inicio y finalización son obligatorias.");
        }
        if (!reserva.getHoraInicio().isBefore(reserva.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de finalización.");
        }
        if (reserva.getCategoriasSolicitadas().isEmpty()) {
            throw new IllegalArgumentException("Debe solicitar al menos una categoría.");
        }
    }

    private boolean hayDisponibilidad(Reserva reserva) {

        /*
         * TODO:
         *
         * 1. Obtener recursos de cada categoría.
         * 2. Revisar las reservas existentes.
         * 3. Comprobar solapamientos.
         * 4. Determinar si existe al menos un recurso disponible
         *    para cada categoría.
         *
         */

        return true;
    }

    private void asignarRecursos(Reserva reserva) {

        /*
         * TODO:
         *
         * Para cada categoría solicitada:
         *
         * 1. Obtene recursos.
         * 2. Busca el primero disponible.
         * 3. Agregar a recursosAsignados.
         */

    }

    private void liberarRecursos(Reserva reserva) {

        /*
         * TODO:
         *
         *
         * Se edita al final, depende del diseño final
         * de Reserva y del XML.
         */

        reserva.limpiarRecursosAsignados();
    }

}

