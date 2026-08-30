package sistema.reservas.service;
import sistema.reservas.dao.RecursoDAO;
import sistema.reservas.model.Recurso;
import java.util.List;


public class RecursoService {
    private final RecursoDAO recursoDAO;

    public RecursoService(RecursoDAO recursoDAO) {
        this.recursoDAO = recursoDAO;
    }

    public void crearRecurso(Recurso recurso) {
        validarRecurso(recurso);
        if (recursoDAO.buscarPorId(recurso.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un recurso con el ID indicado.");
        }
        recursoDAO.guardar(recurso);
    }

    public Recurso buscarRecurso(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del recurso es obligatorio.");
        }

        return recursoDAO.buscarPorId(id);
    }

    public List<Recurso> listarRecursos() {
        return recursoDAO.listar();
    }

    public List<Recurso> listarPorCategoria(int idCategoria) {
        return recursoDAO.listarPorCategoria(idCategoria);
    }

    public void modificarRecurso(Recurso recurso) {
        validarRecurso(recurso);
        if (recursoDAO.buscarPorId(recurso.getId()) == null) {
            throw new IllegalArgumentException("El recurso no existe.");
        }
        recursoDAO.actualizar(recurso);
    }

    public void eliminarRecurso(String id) {
        if (recursoDAO.buscarPorId(id) == null) {
            throw new IllegalArgumentException("El recurso no existe.");
        }
        recursoDAO.eliminar(id);
    }

    private void validarRecurso(Recurso recurso) {
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo.");
        }
        if (recurso.getId() == null || recurso.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del recurso es obligatorio.");
        }
        if (recurso.getCategoria() == null) {
            throw new IllegalArgumentException("El recurso debe tener una categoría.");
        }
        if (recurso.getDescripcion() == null || recurso.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del recurso es obligatoria.");
        }
    }
}
