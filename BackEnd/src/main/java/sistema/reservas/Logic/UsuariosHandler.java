package sistema.reservas.Logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Atiende los codigos de I1 (101-399): usuarios, funcionarios y categorias.
 * Lee del socket en el mismo orden en que escribe el Proxy del Frontend.
 */
public class UsuariosHandler {

    private static final Logger LOG = Logger.getLogger(UsuariosHandler.class.getName());

    private interface Accion { void ejecutar() throws Exception; }
    private interface Consulta { Object ejecutar() throws Exception; }

    private final IService service;

    public UsuariosHandler(IService service) {
        if (service == null) {
            throw new IllegalArgumentException("El service no puede ser nulo.");
        }
        this.service = service;
    }

    /** true = operacion de I1 ya respondida; false = no es de I1 (el Worker sigue con su switch). */
    public boolean atender(int operacion, ObjectInputStream is, ObjectOutputStream os) throws IOException {
        switch (operacion) {
            // ---------- Usuarios ----------
            case Protocol.USUARIO_LOGIN: {
                String username = (String) leer(is);
                String password = (String) leer(is);
                responderConValor(os, () -> service.login(username, password));
                return true;
            }
            case Protocol.USUARIO_CAMBIAR_CLAVE: {
                String username = (String) leer(is);
                String claveActual = (String) leer(is);
                String claveNueva = (String) leer(is);
                responder(os, () -> service.cambiarClave(username, claveActual, claveNueva));
                return true;
            }

            // ---------- Funcionarios ----------
            case Protocol.FUNCIONARIO_CREATE: {
                Object arg = leer(is);
                responder(os, () -> service.create((Funcionario) arg));
                return true;
            }
            case Protocol.FUNCIONARIO_READ: {
                Object arg = leer(is);
                responderConValor(os, () -> service.read((Funcionario) arg));
                return true;
            }
            case Protocol.FUNCIONARIO_UPDATE: {
                Object arg = leer(is);
                responder(os, () -> service.update((Funcionario) arg));
                return true;
            }
            case Protocol.FUNCIONARIO_DELETE: {
                Object arg = leer(is);
                responder(os, () -> service.delete((Funcionario) arg));
                return true;
            }
            case Protocol.FUNCIONARIO_FIND_ALL: {
                Object arg = leer(is);
                responderConValor(os, () -> service.findAll((Funcionario) arg));
                return true;
            }
            case Protocol.FUNCIONARIO_FIND_BY_NOMBRE: {
                Object arg = leer(is);
                responderConValor(os, () -> service.findFuncionariosByNombre((String) arg));
                return true;
            }

            // ---------- Categorias ----------
            case Protocol.CATEGORIA_CREATE: {
                Object arg = leer(is);
                responderConValor(os, () -> service.create((CategoriaRecurso) arg));
                return true;
            }
            case Protocol.CATEGORIA_READ: {
                Object arg = leer(is);
                responderConValor(os, () -> service.read((CategoriaRecurso) arg));
                return true;
            }
            case Protocol.CATEGORIA_UPDATE: {
                Object arg = leer(is);
                responder(os, () -> service.update((CategoriaRecurso) arg));
                return true;
            }
            case Protocol.CATEGORIA_DELETE: {
                Object arg = leer(is);
                responder(os, () -> service.delete((CategoriaRecurso) arg));
                return true;
            }
            case Protocol.CATEGORIA_FIND_ALL: {
                Object arg = leer(is);
                responderConValor(os, () -> service.findAll((CategoriaRecurso) arg));
                return true;
            }
            case Protocol.CATEGORIA_FIND_BY_DESCRIPCION: {
                Object arg = leer(is);
                responderConValor(os, () -> service.findCategoriasByDescripcion((String) arg));
                return true;
            }
            default:
                return false;
        }
    }

    /** Lee SIEMPRE los argumentos completos antes de ejecutar nada, para no desfasar el stream. */
    private Object leer(ObjectInputStream is) throws IOException {
        try {
            return is.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Clase desconocida recibida por el socket", e);
        }
    }

    private void responder(ObjectOutputStream os, Accion accion) throws IOException {
        try {
            accion.ejecutar();
        } catch (Exception ex) {
            enviarError(os, ex);
            return;
        }
        os.writeInt(Protocol.ERROR_NO_ERROR);
        os.flush();
    }

    private void responderConValor(ObjectOutputStream os, Consulta consulta) throws IOException {
        Object resultado;
        try {
            resultado = consulta.ejecutar();
        } catch (Exception ex) {
            enviarError(os, ex);
            return;
        }
        os.writeInt(Protocol.ERROR_NO_ERROR);
        os.writeObject(resultado);
        os.flush();
    }

    private void enviarError(ObjectOutputStream os, Exception ex) throws IOException {
        if (!(ex instanceof IllegalArgumentException)) {
            LOG.log(Level.WARNING, "Fallo una operacion de usuarios/categorias", ex);
        }
        String mensaje = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        os.writeInt(Protocol.ERROR_ERROR);
        os.writeObject(mensaje);
        os.flush();
    }
}