package sistema.reservas.unit;

import sistema.reservas.dao.UsuarioDAO;
import sistema.reservas.model.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO falso en memoria, solo para pruebas unitarias. No toca disco ni XML,
 * así los tests de UsuarioService corren rápido y aislados.
 */
public class UsuarioDAOFalso implements UsuarioDAO {

    private final List<Usuario> usuarios = new ArrayList<>();

    @Override
    public Usuario buscarPorId(int id) {
        for (Usuario u : usuarios) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        for (Usuario u : usuarios) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public void guardar(Usuario usuario) {
        usuarios.add(usuario);
    }

    @Override
    public void actualizar(Usuario usuario) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getId() == usuario.getId()) {
                usuarios.set(i, usuario);
                return;
            }
        }
    }

    @Override
    public void eliminar(int id) {
        usuarios.removeIf(u -> u.getId() == id);
    }

    @Override
    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuarios);
    }
}