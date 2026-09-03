package sistema.reservas.Categoria;

import sistema.reservas.dao.CategoriaRecursoDAO;

import java.util.List;

public class CategoriaRecursoService {

    private final CategoriaRecursoDAO categoriaDAO;

    public CategoriaRecursoService(CategoriaRecursoDAO categoriaDAO) {
        this.categoriaDAO = categoriaDAO;
    }

    public CategoriaRecurso buscarPorId(int id) {
        return categoriaDAO.buscarPorId(id);
    }

    public List<CategoriaRecurso> buscarPorDescripcion(String descripcion) {
        return categoriaDAO.buscarPorDescripcion(descripcion);
    }

    public List<CategoriaRecurso> listarTodas() {
        return categoriaDAO.listarTodos();
    }

    public void crear(CategoriaRecurso categoria) {
        validar(categoria);
        categoriaDAO.guardar(categoria); // el id lo asigna el DAO al persistir (autogenerado)
    }

    public void actualizar(CategoriaRecurso categoria) {
        validar(categoria);
        if (categoriaDAO.buscarPorId(categoria.getId()) == null) {
            throw new IllegalArgumentException("No existe una categoría con ese ID.");
        }
        categoriaDAO.actualizar(categoria);
    }

    public void eliminar(int id) {
        if (categoriaDAO.buscarPorId(id) == null) {
            throw new IllegalArgumentException("No existe una categoría con ese ID.");
        }
        categoriaDAO.eliminar(id);
    }

    private void validar(CategoriaRecurso categoria) {
        if (categoria.getDescripcion() == null || categoria.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
    }
}