package sistema.reservas.Presentation.Login;

import sistema.reservas.Data.persistence.UsuarioXmlPersister;
import sistema.reservas.Data.persistence.UsuariosData;
import sistema.reservas.Logic.Administrador;
import sistema.reservas.Logic.Usuario;

import java.util.List;

/**
 * Los usuarios (Administradores y Funcionarios) se guardan juntos en
 * un solo archivo XML (data/usuarios.xml), leido/escrito con JAXB via
 * UsuarioXmlPersister. Se cargan una sola vez (la primera vez que se
 * usa el Service) y se guardan de nuevo cada vez que algo cambia.
 */
public class UsuarioService {

    private static final UsuarioXmlPersister persister = new UsuarioXmlPersister();
    private static UsuariosData data;

    private static UsuariosData data() {
        if (data == null) {
            try {
                data = persister.load();
            } catch (Exception e) {
                throw new RuntimeException("No se pudo cargar data/usuarios.xml", e);
            }
            if (data.getUsuarios().isEmpty()) {
                // Primera vez que corre el programa: se crea un
                // administrador por defecto para poder entrar.
                // Usuario: admin / Clave: admin
                data.getUsuarios().add(new Administrador(1, "Administrador", "admin", "admin"));
                guardar();
            }
        }
        return data;
    }

    private static void guardar() {
        try {
            persister.store(data);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar data/usuarios.xml", e);
        }
    }

    /**
     * Valida credenciales de login.
     * @return el Usuario si las credenciales son correctas.
     * @throws Exception si el usuario no existe o la clave no coincide.
     */
    public Usuario login(String username, String password) throws Exception {
        for (Usuario u : data().getUsuarios()) {
            if (u.getUsername().equals(username)) {
                if (u.getPassword().equals(password)) {
                    return u;
                }
                throw new Exception("Usuario o clave incorrectos.");
            }
        }
        throw new Exception("Usuario o clave incorrectos.");
    }

    public void cambiarClave(Usuario usuario, String claveActual, String claveNueva) throws Exception {
        if (!usuario.getPassword().equals(claveActual)) {
            throw new Exception("La clave actual no es correcta.");
        }
        if (claveNueva == null || claveNueva.isBlank()) {
            throw new Exception("La nueva clave no puede estar vacía.");
        }
        usuario.setPassword(claveNueva);
        guardar();
    }

    /** Lista completa de usuarios (Administradores + Funcionarios). */
    public static List<Usuario> listarTodos() {
        return data().getUsuarios();
    }

    /** Para que Funcionario (u otros) puedan registrar nuevos usuarios. */
    public static void registrar(Usuario usuario) {
        data().getUsuarios().add(usuario);
        guardar();
    }

    /** Persiste cambios hechos sobre un Usuario ya existente en la lista. */
    public static void guardarCambios() {
        guardar();
    }

    /** Elimina un usuario (ej. al borrar un Funcionario). */
    public static void eliminar(Usuario usuario) {
        data().getUsuarios().remove(usuario);
        guardar();
    }

    /**
     * Limpia el cache en memoria para forzar una relectura del archivo
     * en la siguiente operacion. Solo para pruebas unitarias (JUnit) —
     * la aplicacion real nunca necesita llamar esto.
     */
    public static void resetParaPruebas() {
        data = null;
    }
}