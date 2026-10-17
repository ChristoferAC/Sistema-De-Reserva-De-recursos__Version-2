package sistema.reservas.Logic.Services;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;

import java.util.List;

public interface IServiceRecurso {
    void crearRecurso(Recurso recurso);
    Recurso buscarRecurso(String id);              // null si no existe
    List<Recurso> listarRecursos();
    List<Recurso> listarPorCategoria(int idCategoria);
    void modificarRecurso(Recurso recurso);
    void eliminarRecurso(String id);
    List<CategoriaRecurso> listarCategorias();
}
