package sistema.reservas.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.Administrador.Administrador;
import sistema.reservas.Usuario.Usuario;
import sistema.reservas.Usuario.UsuarioService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioServiceTest {

    private UsuarioDAOFalso usuarioDAO;
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioDAO = new UsuarioDAOFalso();
        usuarioService = new UsuarioService(usuarioDAO);
        usuarioDAO.guardar(new Administrador(1, "Administrador", "admin", "admin123"));
    }

    @Test
    void loginConCredencialesCorrectasDevuelveElUsuario() {
        Usuario usuario = usuarioService.login("admin", "admin123");

        assertNotNull(usuario);
        assertEquals(1, usuario.getId());
    }

    @Test
    void loginConClaveIncorrectaDevuelveNull() {
        assertNull(usuarioService.login("admin", "claveIncorrecta"));
    }

    @Test
    void loginConUsuarioInexistenteDevuelveNull() {
        assertNull(usuarioService.login("noExiste", "cualquierClave"));
    }

    @Test
    void loginConUsuarioVacioDevuelveNull() {
        assertNull(usuarioService.login("", "admin123"));
    }

    @Test
    void cambiarClaveConClaveActualCorrectaActualizaLaClave() {
        Usuario usuario = usuarioDAO.buscarPorId(1);

        usuarioService.cambiarClave(usuario, "admin123", "nuevaClave");

        assertEquals("nuevaClave", usuarioDAO.buscarPorId(1).getPassword());
    }

    @Test
    void cambiarClaveConClaveActualIncorrectaLanzaExcepcion() {
        Usuario usuario = usuarioDAO.buscarPorId(1);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.cambiarClave(usuario, "claveEquivocada", "nuevaClave"));
    }

    @Test
    void cambiarClaveConClaveNuevaVaciaLanzaExcepcion() {
        Usuario usuario = usuarioDAO.buscarPorId(1);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioService.cambiarClave(usuario, "admin123", " "));
    }
}