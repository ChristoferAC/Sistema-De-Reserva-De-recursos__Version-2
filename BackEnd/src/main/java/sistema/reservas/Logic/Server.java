package sistema.reservas.Logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servidor de sockets (Integrante 3): escucha en un puerto y crea un Worker (hilo) por cada
 * cliente que se conecta. No tiene interfaz de usuario.
 */
public class Server {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    private final int puerto;
    private final FabricaContexto fabrica;
    private final List<Worker> workers = new CopyOnWriteArrayList<>();

    private ServerSocket serverSocket;
    private volatile boolean activo;

    public Server(int puerto, FabricaContexto fabrica) {
        if (fabrica == null) {
            throw new IllegalArgumentException("La fabrica de contexto no puede ser nula.");
        }
        this.puerto = puerto;
        this.fabrica = fabrica;
    }

    public synchronized void start() throws IOException {
        if (activo) {
            return;
        }
        serverSocket = new ServerSocket(puerto);   // lanza IOException si el puerto esta ocupado
        activo = true;
        Thread aceptador = new Thread(this::aceptarClientes, "Server-aceptar");
        aceptador.start();
        System.out.println("Servidor escuchando en el puerto " + serverSocket.getLocalPort());
    }

    private void aceptarClientes() {
        while (activo) {
            try {
                Socket socket = serverSocket.accept();
                Worker worker = new Worker(socket, fabrica, this);
                workers.add(worker);
                worker.start();
                System.out.println("Cliente conectado. Quedan: " + workers.size());
            } catch (IOException ex) {
                if (activo) {
                    LOG.log(Level.WARNING, "Error al aceptar un cliente", ex);
                }
            }
        }
    }

    /** Lo llama cada Worker al terminar. */
    void remover(Worker worker) {
        if (workers.remove(worker)) {
            System.out.println("Cliente desconectado. Quedan: " + workers.size());
        }
    }

    public int clientesConectados() {
        return workers.size();
    }

    /** Puerto real (util cuando se crea con puerto 0 en las pruebas). */
    public int getPuerto() {
        return serverSocket != null ? serverSocket.getLocalPort() : puerto;
    }

    public synchronized void stop() {
        activo = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            // ya estaba cerrado
        }
        for (Worker w : workers) {
            w.cerrar();
        }
    }
}