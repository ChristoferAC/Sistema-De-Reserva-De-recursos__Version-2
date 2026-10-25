package sistema.reservas.Data.persistence;

import sistema.reservas.Logic.Usuario;
import java.util.List;

public interface UsuarioDAO {
    Usuario buscarPorCredenciales(String username, String clave);   // null si usuario o clave no coinciden
    Usuario buscarPorId(int id);                                    // null si no existe
    List<Usuario> listar();
    void insertar(Usuario usuario, String clave);                   // el id viene dado (no es AUTO_INCREMENT)
    void actualizar(Usuario usuario);                               // datos, no la clave
    void actualizarClave(int id, String claveNueva);
    void eliminar(int id);
}
