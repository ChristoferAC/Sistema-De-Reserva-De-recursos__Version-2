package sistema.reservas.dao;

import sistema.reservas.Usuario.Usuario;
import java.util.List;

public interface UsuarioDAO {
    Usuario buscarPorId(int id);
    Usuario buscarPorUsername(String username);
    void guardar(Usuario usuario);
    void actualizar(Usuario usuario);
    void eliminar(int id);
    List<Usuario> listarTodos();
}