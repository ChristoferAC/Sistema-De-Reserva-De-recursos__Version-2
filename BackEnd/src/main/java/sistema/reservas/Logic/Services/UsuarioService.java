package sistema.reservas.Logic.Services;

import sistema.reservas.Data.persistence.UsuarioDAO;
import sistema.reservas.Logic.Usuario;
import java.util.List;

/**
 * Login y cambio de clave sobre MySQL (tabla usuario). Una instancia por Worker.
 * Regla 5: la clave se compara dentro de MySQL y todo Usuario que sale de aquí lleva password = null.
 */
public class UsuarioService {

    private static final int MAX_CLAVE = 30;   // usuario.password varchar(30)
    private static final String CREDENCIALES_INVALIDAS = "Usuario o clave incorrectos.";

    private final UsuarioDAO usuarioDAO;

    public UsuarioService(UsuarioDAO usuarioDAO) {
        if (usuarioDAO == null) {
            throw new IllegalArgumentException("El UsuarioDAO no puede ser nulo.");
        }
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * @return el Usuario (Administrador o Funcionario) con password = null.
     * @throws IllegalArgumentException si el usuario no existe o la clave no coincide
     *         (mismo mensaje en ambos casos, para no revelar qué usuarios existen).
     */
    public Usuario login(String username, String password) {
        if (esVacio(username) || password == null) {
            throw new IllegalArgumentException(CREDENCIALES_INVALIDAS);
        }
        Usuario usuario = usuarioDAO.buscarPorCredenciales(username.trim(), password);
        if (usuario == null) {
            throw new IllegalArgumentException(CREDENCIALES_INVALIDAS);
        }
        return usuario;
    }

    /** Valida la clave actual (haciendo login) y hace el UPDATE. */
    public void cambiarClave(String username, String claveActual, String claveNueva) {
        Usuario usuario = login(username, claveActual);
        if (esVacio(claveNueva)) {
            throw new IllegalArgumentException("La nueva clave no puede estar vacía.");
        }
        if (claveNueva.length() > MAX_CLAVE) {
            throw new IllegalArgumentException("La nueva clave no puede superar los " + MAX_CLAVE + " caracteres.");
        }
        usuarioDAO.actualizarClave(usuario.getId(), claveNueva);
    }

    /** null si no existe. */
    public Usuario buscarPorId(int id) {
        return usuarioDAO.buscarPorId(id);
    }

    /** Administradores y funcionarios, todos con password = null. */
    public List<Usuario> listarTodos() {
        return usuarioDAO.listar();
    }

    private boolean esVacio(String texto) {
        return texto == null || texto.isBlank();
    }
}