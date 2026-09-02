package sistema.reservas.Recurso;
import java.util.List;

public class RecursoController {
    private final RecursoService recursoService;

    public RecursoController(RecursoService recursoService) {
        this.recursoService = recursoService;
    }

    public void crear(Recurso recurso) {
        recursoService.crearRecurso(recurso);
    }

    public Recurso buscar(String id) {
        return recursoService.buscarRecurso(id);
    }

    public List<Recurso> listar() {
        return recursoService.listarRecursos();
    }

    public List<Recurso> listarPorCategoria(int idCategoria) {
        return recursoService.listarPorCategoria(idCategoria);
    }

    public void modificar(Recurso recurso) {
        recursoService.modificarRecurso(recurso);
    }

    public void eliminar(String id) {
        recursoService.eliminarRecurso(id);
    }
}
