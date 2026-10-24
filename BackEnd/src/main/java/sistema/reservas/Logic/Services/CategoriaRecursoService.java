// ============================================================
// BACKEND / src/main/java/sistema/reservas/Logic/Services/CategoriaRecursoService.java
// Reemplaza al CategoriaRecursoService XML (FaltaPorMover_O_Borrar). Sin listas estáticas,
// sin XML: la verdad vive en MySQL (regla 6).
//
// Conexión con el resto (en el Service del Backend, con la conexión cx del Worker):
//   CategoriaRecursoService categorias = new CategoriaRecursoService(new CategoriaDAOJdbc(cx));
//   new ServiceRecurso(new RecursoDAOJdbc(cx), categorias::listarTodas);
//   new ServiceReserva(new ReservaDAOJdbc(cx), new RecursoDAOJdbc(cx), categorias::listarTodas, ...);
// ============================================================
package sistema.reservas.Logic.Services;

import sistema.reservas.Data.persistence.CategoriaDAO;
import sistema.reservas.Logic.CategoriaRecurso;

import java.util.List;

/**
 * Categorías de recursos sobre MySQL (tabla categoria). Una instancia por Worker
 * (su DAO usa la conexión de ese Worker).
 */
public class CategoriaRecursoService {

    private static final int MAX_DESCRIPCION = 100;   // categoria.descripcion varchar(100)

    private final CategoriaDAO categoriaDAO;

    public CategoriaRecursoService(CategoriaDAO categoriaDAO) {
        if (categoriaDAO == null) {
            throw new IllegalArgumentException("El CategoriaDAO no puede ser nulo.");
        }
        this.categoriaDAO = categoriaDAO;
    }

    /** Devuelve la categoría con el id que generó MySQL. */
    public CategoriaRecurso crear(CategoriaRecurso categoria) {
        validar(categoria);
        return categoriaDAO.insertar(categoria);
    }

    public void actualizar(CategoriaRecurso categoria) {
        validar(categoria);
        if (categoriaDAO.buscarPorId(categoria.getId()) == null) {
            throw new IllegalArgumentException("No existe una categoría con ese ID.");
        }
        categoriaDAO.actualizar(categoria);
    }

    /** El DAO responde "no existe" o "tiene recursos o reservas asociadas". */
    public void eliminar(int id) {
        categoriaDAO.eliminar(id);
    }

    /** null si no existe. */
    public CategoriaRecurso buscarPorId(int id) {
        return categoriaDAO.buscarPorId(id);
    }

    public List<CategoriaRecurso> buscarPorDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return categoriaDAO.listar();
        }
        return categoriaDAO.buscarPorDescripcion(descripcion.trim());
    }

    public List<CategoriaRecurso> listarTodas() {
        return categoriaDAO.listar();
    }

    private void validar(CategoriaRecurso categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula.");
        }
        if (categoria.getDescripcion() == null || categoria.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        categoria.setDescripcion(categoria.getDescripcion().trim());
        if (categoria.getDescripcion().length() > MAX_DESCRIPCION) {
            throw new IllegalArgumentException(
                    "La descripción no puede superar los " + MAX_DESCRIPCION + " caracteres.");
        }
        // La categoría se identifica por su descripción (como en categorias.xml). Si 'nombre'
        // se guardara copiado de la descripción, quedaría desactualizado al editarla y
        // Estadísticas/Calendarización (que prefieren 'nombre') mostrarían el texto viejo.
        categoria.setNombre("");
    }
}