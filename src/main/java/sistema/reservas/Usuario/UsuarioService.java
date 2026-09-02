package sistema.reservas.Usuario;

import sistema.reservas.dao.UsuarioDAO;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Valida credenciales de login.
     * @return el Usuario si las credenciales son correctas, null si no.
     */
    public Usuario login(String username, String password) {
        if (username == null || username.isBlank() || password == null) {
            return null;
        }
        Usuario usuario = usuarioDAO.buscarPorUsername(username);
        if (usuario == null) {
            return null;
        }
        return usuario.getPassword().equals(password) ? usuario : null;
    }

    public void cambiarClave(Usuario usuario, String claveActual, String claveNueva) {
        if (!usuario.getPassword().equals(claveActual)) {
            throw new IllegalArgumentException("La clave actual no es correcta.");
        }
        if (claveNueva == null || claveNueva.isBlank()) {
            throw new IllegalArgumentException("La nueva clave no puede estar vacía.");
        }
        usuario.setPassword(claveNueva);
        usuarioDAO.actualizar(usuario);
    }
}