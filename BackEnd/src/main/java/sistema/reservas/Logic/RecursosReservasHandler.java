package sistema.reservas.Logic;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecursosReservasHandler {

    private static final Logger LOG = Logger.getLogger(RecursosReservasHandler.class.getName());

    private interface Accion { void ejecutar() throws Exception; }
    private interface Consulta { Object ejecutar() throws Exception; }

    private final IService service;

    public RecursosReservasHandler(IService service) {
        if (service == null) {
            throw new IllegalArgumentException("El service no puede ser nulo.");
        }
        this.service = service;
    }

    /** true = operación de I2 ya respondida; false = no es de I2 (el Worker sigue con su switch). */
    public boolean atender(int operacion, ObjectInputStream is, ObjectOutputStream os) throws IOException {
        switch (operacion) {
            case Protocol.RECURSO_CREATE: {
                Object arg = leer(is);
                responder(os, () -> service.create((Recurso) arg));
                return true;
            }
            case Protocol.RECURSO_READ: {
                Object arg = leer(is);
                responderConValor(os, () -> service.read((Recurso) arg));
                return true;
            }
            case Protocol.RECURSO_UPDATE: {
                Object arg = leer(is);
                responder(os, () -> service.update((Recurso) arg));
                return true;
            }
            case Protocol.RECURSO_DELETE: {
                Object arg = leer(is);
                responder(os, () -> service.delete((Recurso) arg));
                return true;
            }
            case Protocol.RECURSO_FIND_ALL: {
                Object arg = leer(is);
                responderConValor(os, () -> service.findAll((Recurso) arg));
                return true;
            }
            case Protocol.RECURSO_FIND_BY_CATEGORIA: {
                Object arg = leer(is);
                responderConValor(os, () -> service.findRecursosByCategoria((CategoriaRecurso) arg));
                return true;
            }
            case Protocol.RESERVA_CREATE: {
                Object arg = leer(is);
                responderConValor(os, () -> service.create((Reserva) arg));
                return true;
            }
            case Protocol.RESERVA_UPDATE: {
                Object arg = leer(is);
                responderConValor(os, () -> service.update((Reserva) arg));
                return true;
            }
            case Protocol.RESERVA_CANCELAR: {
                Object arg = leer(is);
                responder(os, () -> service.cancelar((Reserva) arg));
                return true;
            }
            case Protocol.RESERVA_FIND_ALL: {
                Object arg = leer(is);
                responderConValor(os, () -> service.findAll((Reserva) arg));
                return true;
            }
            case Protocol.RESERVA_FIND_BY_FUNCIONARIO: {
                Object arg = leer(is);
                responderConValor(os, () -> service.findReservasByFuncionario((Funcionario) arg));
                return true;
            }
            case Protocol.RESERVA_EXTRAER_IA: {
                Object arg = leer(is);
                responderConValor(os, () -> service.extraerConIA((String) arg));
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
