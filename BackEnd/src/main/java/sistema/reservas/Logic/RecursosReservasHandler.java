package sistema.reservas.Logic;

import sistema.reservas.Data.persistence.CategoriaDAOJdbc;
import sistema.reservas.Data.persistence.RecursoDAOJdbc;
import sistema.reservas.Data.persistence.ReservaDAOJdbc;
import sistema.reservas.Logic.Services.CategoriaRecursoService;
import sistema.reservas.Logic.Services.Notificador;
import sistema.reservas.Logic.Services.ServiceRecurso;
import sistema.reservas.Logic.Services.ServiceReserva;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecursosReservasHandler implements Handler {

    private static final Logger LOG = Logger.getLogger(RecursosReservasHandler.class.getName());

    private interface Accion { void ejecutar() throws Exception; }
    private interface Consulta { Object ejecutar() throws Exception; }

    private final ServiceRecurso recursos;
    private final ServiceReserva reservas;

    /** Una conexión por Worker.*/
    public RecursosReservasHandler(Connection conexion, Worker worker, Notificador notificador) {
        CategoriaRecursoService categorias = new CategoriaRecursoService(new CategoriaDAOJdbc(conexion));
        this.recursos = new ServiceRecurso(new RecursoDAOJdbc(conexion), categorias::listarTodas);
        this.reservas = new ServiceReserva(new ReservaDAOJdbc(conexion), new RecursoDAOJdbc(conexion),
                categorias::listarTodas, notificador, worker);
    }


    public RecursosReservasHandler(Connection conexion, Worker worker) {
        this(conexion, worker, null);
    }

    /** Para pruebas: services ya armados (con DAOs falsos, sin MySQL). */
    public RecursosReservasHandler(ServiceRecurso recursos, ServiceReserva reservas) {
        if (recursos == null || reservas == null) {
            throw new IllegalArgumentException("Los services no pueden ser nulos.");
        }
        this.recursos = recursos;
        this.reservas = reservas;
    }

    /** true = operación de I2 ya respondida; false = no es de I2 (no se lee nada del stream). */
    @Override
    public boolean atender(int operacion, ObjectInputStream is, ObjectOutputStream os) throws IOException {
        switch (operacion) {

            // ---------- Recursos ----------
            case Protocol.RECURSO_CREATE: {
                Recurso recurso = (Recurso) leer(is);
                responder(os, () -> recursos.crearRecurso(recurso));
                return true;
            }
            case Protocol.RECURSO_READ: {
                Recurso recurso = (Recurso) leer(is);
                responderConValor(os, () -> {
                    Recurso encontrado = recursos.buscarRecurso(requerido(recurso, "El recurso es obligatorio.").getId());
                    if (encontrado == null) {
                        throw new IllegalArgumentException("El recurso no existe.");
                    }
                    return encontrado;
                });
                return true;
            }
            case Protocol.RECURSO_UPDATE: {
                Recurso recurso = (Recurso) leer(is);
                responder(os, () -> recursos.modificarRecurso(recurso));
                return true;
            }
            case Protocol.RECURSO_DELETE: {
                Recurso recurso = (Recurso) leer(is);
                responder(os, () -> recursos.eliminarRecurso(requerido(recurso, "El recurso es obligatorio.").getId()));
                return true;
            }
            case Protocol.RECURSO_FIND_ALL: {
                leer(is);   // el filtro solo desambigua el tipo
                responderConValor(os, recursos::listarRecursos);
                return true;
            }
            case Protocol.RECURSO_FIND_BY_CATEGORIA: {
                CategoriaRecurso categoria = (CategoriaRecurso) leer(is);
                responderConValor(os, () -> recursos.listarPorCategoria(
                        requerido(categoria, "La categoría es obligatoria.").getId()));
                return true;
            }

            // ---------- Reservas ----------
            case Protocol.RESERVA_CREATE: {
                Reserva reserva = (Reserva) leer(is);
                responderConValor(os, () -> reservas.crearReserva(reserva));
                return true;
            }
            case Protocol.RESERVA_UPDATE: {
                Reserva reserva = (Reserva) leer(is);
                responderConValor(os, () -> reservas.modificarReserva(reserva));
                return true;
            }
            case Protocol.RESERVA_CANCELAR: {
                Reserva reserva = (Reserva) leer(is);
                responder(os, () -> reservas.cancelarReserva(requerido(reserva, "La reserva es obligatoria.").getId()));
                return true;
            }
            case Protocol.RESERVA_FIND_ALL: {
                leer(is);
                responderConValor(os, reservas::listarReservas);
                return true;
            }
            case Protocol.RESERVA_FIND_BY_FUNCIONARIO: {
                Funcionario funcionario = (Funcionario) leer(is);
                responderConValor(os, () -> reservas.listarReservasFuncionario(
                        requerido(funcionario, "El funcionario es obligatorio.").getId()));
                return true;
            }
            case Protocol.RESERVA_EXTRAER_IA: {
                String frase = (String) leer(is);
                responderConValor(os, () -> reservas.extraerReserva(frase));
                return true;
            }
            default:
                return false;
        }
    }

    /** Lee SIEMPRE el argumento completo antes de ejecutar nada, para no desfasar el stream. */
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
            LOG.log(Level.WARNING, "Falló una operación de recursos/reservas", ex);
        }
        String mensaje = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        os.writeInt(Protocol.ERROR_ERROR);
        os.writeObject(mensaje);
        os.flush();
    }
}