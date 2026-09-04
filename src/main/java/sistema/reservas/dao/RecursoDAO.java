package sistema.reservas.dao;
import sistema.reservas.Logic.Recurso.Recurso;
import java.util.List;

public interface RecursoDAO {

    void guardar(Recurso recurso);

    Recurso buscarPorId(String id);

    List<Recurso> listar();

    List<Recurso> listarPorCategoria(int idCategoria);

    void actualizar(Recurso recurso);

    void eliminar(String id);
}


/*
    *** Este se conecta despues con el XML compartido ***
*/