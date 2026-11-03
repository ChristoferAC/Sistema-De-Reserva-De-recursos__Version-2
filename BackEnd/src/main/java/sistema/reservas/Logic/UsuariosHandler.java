package sistema.reservas.Logic;

import sistema.reservas.Data.persistence.CategoriaDAOJdbc;
import sistema.reservas.Data.persistence.UsuarioDAOJdbc;
import sistema.reservas.Logic.Services.CategoriaRecursoService;
import sistema.reservas.Logic.Services.FuncionarioService;
import sistema.reservas.Logic.Services.UsuarioService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Atiende los codigos de I1 (101-399): usuarios, funcionarios y categorias.
 * Lee del socket en el mismo orden en que escribe el Proxy del Frontend.
 */
public class UsuariosHandler implements Handler {

    private static final Logger LOG = Logger.getLogger(UsuariosHandler.class.getName());

    private interface Accion { void ejecutar() throws Exception; }
    private interface Consulta { Object ejecutar() throws Exception; }

    private final UsuarioService usuarios;
    private final FuncionarioService funcionarios;
    private final CategoriaRecursoService categorias;

    /** Una conexion por Worker. */
    public UsuariosHandler(Connection conexion) {
        UsuarioDAOJdbc usuarioDAO = new UsuarioDAOJdbc(conexion);
        this.usuarios = new UsuarioService(usuarioDAO);
        this.funcionarios = new FuncionarioService(usuarioDAO);
        this.categorias = new CategoriaRecursoService(new CategoriaDAOJdbc(conexion));
    }

    /** Para pruebas: services ya armados (con DAOs falsos, sin MySQL). */
    public UsuariosHandler(UsuarioService usuarios, FuncionarioService funcionarios, CategoriaRecursoService categorias) {
        if (usuarios == null || funcionarios == null || categorias == null) {
            throw new IllegalArgumentException("Los services no pueden ser nulos.");
        }
        this.usuarios = usuarios;
        this.funcionarios = funcionarios;
        this.categorias = categorias;
    }

    /** true = operacion de I1 ya respondida; false = no es de I1 (no se lee nada del stream). */
    @Override
    public boolean atender(int operacion, ObjectInputStream is, ObjectOutputStream os) throws IOException {
        switch (operacion) {

            // ---------- Usuarios ----------
            case Protocol.USUARIO_LOGIN: {
                String username = (String) leer(is);
                String password = (String) leer(is);
                responderConValor(os, () -> usuarios.login(username, password));
                return true;
            }
            case Protocol.USUARIO_CAMBIAR_CLAVE: {
                String username = (String) leer(is);
                String claveActual = (String) leer(is);
                String claveNueva = (String) leer(is);
                responder(os, () -> usuarios.cambiarClave(username, claveActual, claveNueva));
                return true;
            }

            // ---------- Funcionarios ----------
            case Protocol.FUNCIONARIO_CREATE: {
                Funcionario funcionario = (Funcionario) leer(is);
                responder(os, () -> funcionarios.crear(funcionario));
                return true;
            }
            case Protocol.FUNCIONARIO_READ: {
                Funcionario funcionario = (Funcionario) leer(is);
                responderConValor(os, () -> {
                    Funcionario encontrado = funcionarios.buscarPorId(requerido(funcionario, "El funcionario es obligatorio.").getId());
                    if (encontrado == null) {
                        throw new IllegalArgumentException("No existe un funcionario con ese ID.");
                    }
                    return encontrado;
                });
                return true;
            }
            case Protocol.FUNCIONARIO_UPDATE: {
                Funcionario funcionario = (Funcionario) leer(is);
                responder(os, () -> funcionarios.actualizar(funcionario));
                return true;
            }
            case Protocol.FUNCIONARIO_DELETE: {
                Funcionario funcionario = (Funcionario) leer(is);
                responder(os, () -> funcionarios.eliminar(requerido(funcionario, "El funcionario es obligatorio.").getId()));
                return true;
            }
            case Protocol.FUNCIONARIO_FIND_ALL: {
                leer(is);   // el filtro solo sirve para elegir el metodo en el Proxy
                responderConValor(os, () -> funcionarios.listarTodos());
                return true;
            }
            case Protocol.FUNCIONARIO_FIND_BY_NOMBRE: {
                String nombre = (String) leer(is);
                responderConValor(os, () -> funcionarios.buscarPorNombre(nombre));
                return true;
            }

            // ---------- Categorias ----------
            case Protocol.CATEGORIA_CREATE: {
                CategoriaRecurso categoria = (CategoriaRecurso) leer(is);
                responderConValor(os, () -> categorias.crear(categoria));
                return true;
            }
            case Protocol.CATEGORIA_READ: {
                CategoriaRecurso categoria = (CategoriaRecurso) leer(is);
                responderConValor(os, () -> {
                    CategoriaRecurso encontrada = categorias.buscarPorId(requerido(categoria, "La categoría es obligatoria.").getId());
                    if (encontrada == null) {
                        throw new IllegalArgumentException("No existe una categoría con ese ID.");
                    }
                    return encontrada;
                });
                return true;
            }
            case Protocol.CATEGORIA_UPDATE: {
                CategoriaRecurso categoria = (CategoriaRecurso) leer(is);
                responder(os, () -> categorias.actualizar(categoria));
                return true;
            }
            case Protocol.CATEGORIA_DELETE: {
                CategoriaRecurso categoria = (CategoriaRecurso) leer(is);
                responder(os, () -> categorias.eliminar(requerido(categoria, "La categoría es obligatoria.").getId()));
                return true;
            }
            case Protocol.CATEGORIA_FIND_ALL: {
                leer(is);   // el filtro solo sirve para elegir el metodo en el Proxy
                responderConValor(os, () -> categorias.listarTodas());
                return true;
            }
            case Protocol.CATEGORIA_FIND_BY_DESCRIPCION: {
                String descripcion = (String) leer(is);
                responderConValor(os, () -> categorias.buscarPorDescripcion(descripcion));
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

    private static <T> T requerido(T valor, String mensaje) {
        if (valor == null) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor;
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