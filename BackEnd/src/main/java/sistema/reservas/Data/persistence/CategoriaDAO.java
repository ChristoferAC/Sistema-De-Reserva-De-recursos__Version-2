package sistema.reservas.Data.persistence;

import sistema.reservas.Logic.CategoriaRecurso;
import java.util.List;

public interface  CategoriaDAO {
    CategoriaRecurso insertar(CategoriaRecurso categoria);   // MySQL genera el id y queda en el objeto devuelto
    CategoriaRecurso buscarPorId(int id);                    // null si no existe
    List<CategoriaRecurso> buscarPorDescripcion(String texto);
    List<CategoriaRecurso> listar();
    void actualizar(CategoriaRecurso categoria);
    void eliminar(int id);
}
