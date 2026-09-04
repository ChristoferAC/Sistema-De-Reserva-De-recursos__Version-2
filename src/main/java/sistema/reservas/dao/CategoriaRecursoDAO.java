package sistema.reservas.dao;

import sistema.reservas.Logic.Categoria.CategoriaRecurso;
import java.util.List;

public interface CategoriaRecursoDAO {
    CategoriaRecurso buscarPorId(int id);
    List<CategoriaRecurso> buscarPorDescripcion(String descripcion);
    void guardar(CategoriaRecurso categoria);
    void actualizar(CategoriaRecurso categoria);
    void eliminar(int id);
    List<CategoriaRecurso> listarTodos();
}