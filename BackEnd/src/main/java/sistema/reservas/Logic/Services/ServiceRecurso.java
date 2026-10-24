// ============================================================
// BACKEND / src/main/java/sistema/reservas/Logic/Services/ServiceRecurso.java
// (la carpeta debe llamarse "Services", igual que el paquete)
// Sin IServiceRecurso: ya no existe. Los nombres de los métodos no cambian.
// ============================================================
package sistema.reservas.Logic.Services;

import sistema.reservas.Data.persistence.RecursoDAO;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;

import java.util.List;
import java.util.function.Supplier;

public class ServiceRecurso {

    private final RecursoDAO recursoDAO;
    private final Supplier<List<CategoriaRecurso>> proveedorCategorias;

    /**
     * @param proveedorCategorias de dónde salen las categorías (H7): se cablea con el
     *        CategoriaDAO de I1 (p. ej. categoriaDAO::listarTodos). Sin copia propia.
     */
    public ServiceRecurso(RecursoDAO recursoDAO, Supplier<List<CategoriaRecurso>> proveedorCategorias) {
        if (recursoDAO == null) {
            throw new IllegalArgumentException("El RecursoDAO no puede ser nulo.");
        }
        if (proveedorCategorias == null) {
            throw new IllegalArgumentException("El proveedor de categorías no puede ser nulo.");
        }
        this.recursoDAO = recursoDAO;
        this.proveedorCategorias = proveedorCategorias;
    }

    public void crearRecurso(Recurso recurso) {
        validarRecurso(recurso);
        if (recursoDAO.buscarPorId(recurso.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un recurso con el ID indicado.");
        }
        recursoDAO.insertar(recurso);
    }

    /** null si no existe (el Service del Backend lo traduce a "El recurso no existe."). */
    public Recurso buscarRecurso(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del recurso es obligatorio.");
        }
        return recursoDAO.buscarPorId(id.trim());
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
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del recurso es obligatorio.");
        }
        recursoDAO.eliminar(id.trim());   // el DAO responde "no existe" o "tiene reservas asociadas"
    }

    public List<CategoriaRecurso> listarCategorias() {
        return proveedorCategorias.get();
    }

    private void validarRecurso(Recurso recurso) {
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo.");
        }
        if (recurso.getId() == null || recurso.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del recurso es obligatorio.");
        }
        if (recurso.getNombre() == null || recurso.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del recurso es obligatorio.");
        }
        if (recurso.getDescripcion() == null || recurso.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del recurso es obligatoria.");
        }
        if (recurso.getCategoria() == null) {
            throw new IllegalArgumentException("El recurso debe tener una categoría.");
        }
        recurso.setId(recurso.getId().trim());
        recurso.setNombre(recurso.getNombre().trim());
        recurso.setDescripcion(recurso.getDescripcion().trim());
    }
}