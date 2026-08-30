package sistema.reservas.service;

import sistema.reservas.dao.RecursoDAO;
import sistema.reservas.dao.ReservaDAO;
import sistema.reservas.model.CategoriaRecurso;
import sistema.reservas.model.Recurso;
import sistema.reservas.model.Reserva;
import java.util.ArrayList;
import java.util.List;

public class ReservaService {

    private final ReservaDAO reservaDAO;
    private final RecursoDAO recursoDAO;

    public ReservaService(ReservaDAO reservaDAO, RecursoDAO recursoDAO) {
        if (reservaDAO == null) {throw new IllegalArgumentException("El ReservaDAO no puede ser nulo.");
        }
        if (recursoDAO == null) {throw new IllegalArgumentException("El RecursoDAO no puede ser nulo.");
        }
        this.reservaDAO = reservaDAO;
        this.recursoDAO = recursoDAO;
    }

    public void crearReserva(Reserva reserva) {
        validarReserva(reserva);
        if (reservaDAO.buscarPorId(reserva.getId()) != null) {
            throw new IllegalArgumentException("Ya existe una reserva con el ID indicado.");
        }
        asignarRecursosDisponibles(reserva, -1);
        reservaDAO.guardar(reserva);
    }

    public Reserva buscarReserva(int id) {return reservaDAO.buscarPorId(id);}

    public List<Reserva> listarReservas() {return reservaDAO.listar();}

    public List<Reserva> listarReservasFuncionario(int idFuncionario) {
        return reservaDAO.listarPorFuncionario(idFuncionario);
    }

    public List<CategoriaRecurso> obtenerCategoriasSinDisponibilidad(Reserva reserva) {
        validarDatosDeDisponibilidad(reserva);
        return obtenerCategoriasSinDisponibilidadExcluyendo(reserva, -1);
    }

    public boolean hayDisponibilidad(Reserva reserva) {
        return obtenerCategoriasSinDisponibilidad(reserva).isEmpty();
    }

    public void modificarReserva(Reserva reserva) {
        validarReserva(reserva);
        Reserva reservaExistente = reservaDAO.buscarPorId(reserva.getId());
        if (reservaExistente == null) {
            throw new IllegalArgumentException("La reserva no existe.");
        }
        asignarRecursosDisponibles(reserva, reserva.getId());
        reservaDAO.actualizar(reserva);
    }

    public void cancelarReserva(int id) {

        Reserva reserva = reservaDAO.buscarPorId(id);

        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no existe.");
        }
        reserva.limpiarRecursosAsignados();
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
            throw new IllegalArgumentException("La hora de inicio debe ser anterior " + "a la hora de finalización.");
        }

        if (reserva.getCategoriasSolicitadas() == null || reserva.getCategoriasSolicitadas().isEmpty()) {
            throw new IllegalArgumentException("Debe solicitar al menos una categoría.");
        }
        validarCategorias(reserva.getCategoriasSolicitadas());
    }

    private void validarCategorias(
            List<CategoriaRecurso> categorias) {

        for (int i = 0; i < categorias.size(); i++) {
            CategoriaRecurso categoria = categorias.get(i);
            if (categoria == null) {
                throw new IllegalArgumentException("La reserva no puede contener categorías nulas.");
            }

            for (int j = i + 1; j < categorias.size(); j++) {
                CategoriaRecurso otraCategoria = categorias.get(j);
                if (otraCategoria != null && categoria.getId() == otraCategoria.getId()) {

                    throw new IllegalArgumentException("La categoría '" + categoria.getNombre() + "' fue solicitada más de una vez.");
                }
            }
        }
    }

    private void validarDatosDeDisponibilidad(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }

        if (reserva.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        if (reserva.getHoraInicio() == null || reserva.getHoraFin() == null) {
            throw new IllegalArgumentException("Las horas de inicio y finalización son obligatorias.");
        }
        if (!reserva.getHoraInicio().isBefore(reserva.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior " + "a la hora de finalización.");
        }

        if (reserva.getCategoriasSolicitadas() == null || reserva.getCategoriasSolicitadas().isEmpty()) {
            throw new IllegalArgumentException("Debe solicitar al menos una categoría.");
        }
    }

    private List<CategoriaRecurso>
    obtenerCategoriasSinDisponibilidadExcluyendo(Reserva reserva, int idReservaExcluida) {
        validarDatosDeDisponibilidad(reserva);
        List<CategoriaRecurso> categoriasSinDisponibilidad = new ArrayList<>();

        List<Reserva> reservasDelDia = reservaDAO.listarPorFecha(reserva.getFecha().toString());

        for (CategoriaRecurso categoria : reserva.getCategoriasSolicitadas()) {
            List<Recurso> recursos = recursoDAO.listarPorCategoria(categoria.getId());
            boolean disponible = false;
            for (Recurso recurso : recursos) {
                if (recursoDisponible(recurso, reserva, reservasDelDia, idReservaExcluida)) {
                    disponible = true;
                    break;
                }
            }
            if (!disponible) {
                categoriasSinDisponibilidad.add(categoria);
            }
        }
        return categoriasSinDisponibilidad;
    }

    private void asignarRecursosDisponibles(Reserva reserva, int idReservaExcluida) {
        List<CategoriaRecurso> categoriasSinDisponibilidad = obtenerCategoriasSinDisponibilidadExcluyendo(reserva, idReservaExcluida);
        if (!categoriasSinDisponibilidad.isEmpty()) {
            throw new IllegalArgumentException(construirMensajeDisponibilidad(categoriasSinDisponibilidad));
        }

        List<Reserva> reservasDelDia = reservaDAO.listarPorFecha(reserva.getFecha().toString());

        List<Recurso> nuevosRecursos = new ArrayList<>();

        for (CategoriaRecurso categoria : reserva.getCategoriasSolicitadas()) {

            List<Recurso> recursos = recursoDAO.listarPorCategoria(categoria.getId());

            for (Recurso recurso : recursos) {
                if (recursoDisponible(recurso, reserva, reservasDelDia, idReservaExcluida)) {
                    nuevosRecursos.add(recurso);
                    break;
                }
            }
        }

        reserva.limpiarRecursosAsignados();

        for (Recurso recurso : nuevosRecursos) {
            reserva.agregarRecurso(recurso);
        }
    }

    private boolean recursoDisponible(Recurso recurso, Reserva reserva, List<Reserva> reservasDelDia) {
        return recursoDisponible(recurso, reserva, reservasDelDia, -1);
    }

    private boolean recursoDisponible(Recurso recurso, Reserva reserva, List<Reserva> reservasDelDia, int idReservaExcluida) {
        if (recurso == null) {
            return false;
        }

        for (Reserva otraReserva : reservasDelDia) {

            if (otraReserva == null) {continue;}
            if (otraReserva.getId() == idReservaExcluida) {continue;}
            if (tieneRecurso(otraReserva, recurso.getId())) {
                if (haySolapamiento(reserva, otraReserva)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean tieneRecurso(Reserva reserva, String idRecurso) {
        if (reserva.getRecursosAsignados() == null) {
            return false;
        }
        for (Recurso recurso : reserva.getRecursosAsignados()) {
            if (recurso != null && idRecurso.equals(recurso.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean haySolapamiento(Reserva reserva1, Reserva reserva2) {
        if (!reserva1.getFecha().equals(reserva2.getFecha())) {
            return false;
        }
        return reserva1.getHoraInicio().isBefore(reserva2.getHoraFin()) && reserva2.getHoraInicio().isBefore(reserva1.getHoraFin());
    }

    private String construirMensajeDisponibilidad(List<CategoriaRecurso> categorias) {
        StringBuilder mensaje = new StringBuilder("No hay disponibilidad para: ");

        for (int i = 0; i < categorias.size(); i++) {
            mensaje.append(categorias.get(i).getNombre());
            if (i < categorias.size() - 1) {
                mensaje.append(", ");
            }
        }
        mensaje.append(".");
        return mensaje.toString();
    }
}