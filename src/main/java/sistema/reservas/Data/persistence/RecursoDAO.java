package sistema.reservas.Data.persistence;

import sistema.reservas.Logic.Recurso;

import java.util.List;

public interface RecursoDAO {
    void insertar(Recurso recurso);
    Recurso buscarPorId(String id);                 // null si no existe
    List<Recurso> listar();
    List<Recurso> listarPorCategoria(int idCategoria);
    void actualizar(Recurso recurso);
    void eliminar(String id);
}