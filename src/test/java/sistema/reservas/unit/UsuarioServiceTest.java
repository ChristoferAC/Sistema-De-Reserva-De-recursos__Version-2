package sistema.reservas.unit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.Logic.Usuario;
import sistema.reservas.Presentation.Login.UsuarioService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioServiceTest {

    private UsuarioService usuarioService;

    @BeforeAll
    static void respaldarDatosReales() {
        XmlTestDataSupport.respaldar();
    }

    @AfterAll
    static void restaurarDatosReales() {
        XmlTestDataSupport.restaurar();
    }

    @BeforeEach
    void setUp() {
        XmlTestDataSupport.limpiar();
        UsuarioService.resetParaPruebas();
        usuarioService = new UsuarioService();
    }

    @AfterEach
    void tearDown() {
        XmlTestDataSupport.limpiar();
        UsuarioService.resetParaPruebas();
    }

    @Test
    void primeraVezSiembraUnAdministradorPorDefecto() throws Exception {
        Usuario usuario = usuarioService.login("admin", "admin");

        assertNotNull(usuario);
        assertEquals("ADMIN", usuario.getRol());
    }

    @Test
    void loginConClaveIncorrectaLanzaExcepcion() {
        assertThrows(Exception.class, () -> usuarioService.login("admin", "claveIncorrecta"));
    }

    @Test
    void loginConUsuarioInexistenteLanzaExcepcion() {
        assertThrows(Exception.class, () -> usuarioService.login("noExiste", "cualquierClave"));
    }

    @Test
    void cambiarClaveConClaveActualCorrectaActualizaLaClave() throws Exception {
        Usuario admin = usuarioService.login("admin", "admin");

        usuarioService.cambiarClave(admin, "admin", "nuevaClave");

        assertNotNull(usuarioService.login("admin", "nuevaClave"));
    }

    @Test
    void cambiarClaveConClaveActualIncorrectaLanzaExcepcion() throws Exception {
        Usuario admin = usuarioService.login("admin", "admin");

        assertThrows(Exception.class,
                () -> usuarioService.cambiarClave(admin, "claveEquivocada", "nuevaClave"));
    }

    @Test
    void cambiarClaveConClaveNuevaVaciaLanzaExcepcion() throws Exception {
        Usuario admin = usuarioService.login("admin", "admin");

        assertThrows(Exception.class,
                () -> usuarioService.cambiarClave(admin, "admin", " "));
    }
}
