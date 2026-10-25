package sistema.reservas.Logic;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Un hilo por cliente conectado (Integrante 3).
 *
 * Protocolo (el mismo que ya cumple el Service proxy del Frontend):
 *   Peticion : int codigo + los argumentos que cada operacion defina (1, 2, 3 objetos...)
 *   Respuesta: int ERROR_NO_ERROR [+ objeto resultado]   |   int ERROR_ERROR + String mensaje
 * El Worker solo lee el codigo; los argumentos y la respuesta los maneja el Handler del modulo.
 */
public class Worker implements Runnable {

    private static final Logger LOG = Logger.getLogger(Worker.class.getName());

    private final Socket socket;
    private final FabricaContexto fabrica;
    private final Server server;
    private Thread hilo;

    private ObjectOutputStream os;
    private ObjectInputStream is;

    public Worker(Socket socket, FabricaContexto fabrica, Server server) {
        this.socket = socket;
        this.fabrica = fabrica;
        this.server = server;
    }

    public void start() {
        hilo = new Thread(this, "Worker-" + socket.getRemoteSocketAddress());
        hilo.start();
    }

    /** Lo usa el Server al apagarse: cerrar el socket destraba el readInt() bloqueado. */
    public void cerrar() {
        try {
            socket.close();
        } catch (IOException ex) {
            // ya estaba cerrado
        }
    }

    @Override
    public void run() {
        ContextoCliente contexto = null;
        String falloContexto = null;
        try {
            // Mismo orden que el Frontend: primero el de salida, luego el de entrada.
            os = new ObjectOutputStream(socket.getOutputStream());
            os.flush();
            is = new ObjectInputStream(socket.getInputStream());

            // Si MySQL esta caido, el cliente igual se conecta y cada peticion recibe el motivo.
            try {
                contexto = fabrica.crear(this);
            } catch (Exception ex) {
                falloContexto = "No se pudo preparar la conexion con la base de datos: " + mensaje(ex);
                LOG.log(Level.SEVERE, falloContexto, ex);
            }

            atenderPeticiones(contexto, falloContexto);

        } catch (IOException ex) {
            if (!socket.isClosed()) {
                LOG.log(Level.FINE, "Conexion terminada: " + ex.getMessage());
            }
        } finally {
            if (contexto != null) {
                try {
                    contexto.close();
                } catch (Exception ex) {
                    LOG.log(Level.WARNING, "Error al cerrar el contexto del cliente", ex);
                }
            }
            cerrar();
            server.remover(this);
        }
    }

    private void atenderPeticiones(ContextoCliente contexto, String falloContexto) throws IOException {
        while (true) {
            int codigo;
            try {
                codigo = is.readInt();
            } catch (EOFException | SocketException ex) {
                return;   // el cliente se fue sin avisar o el Server cerro el socket
            }
            if (codigo == Protocol.DISCONNECT) {
                return;
            }

            // Sin contexto (ej. MySQL caido) o con un codigo desconocido no se sabe cuantos
            // argumentos vienen detras: se responde el motivo y se cierra la conexion.
            if (falloContexto != null) {
                enviarError(falloContexto);
                return;
            }

            os.reset();   // sin reset, ObjectOutputStream reenvia como referencia objetos ya enviados
            if (!despachar(contexto, codigo)) {
                enviarError("Operacion no soportada por el servidor (codigo " + codigo + ").");
                return;
            }
        }
    }

    /** Le pregunta a cada handler hasta que uno acepte el codigo. */
    private boolean despachar(ContextoCliente contexto, int codigo) throws IOException {
        for (Handler handler : contexto.handlers()) {
            if (handler.atender(codigo, is, os)) {
                return true;
            }
        }
        return false;
    }

    private void enviarError(String texto) throws IOException {
        os.reset();
        os.writeInt(Protocol.ERROR_ERROR);
        os.writeObject(texto);
        os.flush();
    }

    private static String mensaje(Throwable ex) {
        String m = ex.getMessage();
        return (m == null || m.isBlank()) ? ex.getClass().getSimpleName() : m;
    }
}