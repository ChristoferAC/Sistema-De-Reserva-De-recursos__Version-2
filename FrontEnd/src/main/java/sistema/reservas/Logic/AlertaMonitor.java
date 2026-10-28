package sistema.reservas.Logic;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Canal asincronico de alertas del Frontend (equivalente al FsMonitor del profe).
 * Se abre DESPUES del login: se conecta al puerto de alertas del Backend, se identifica con el id
 * del usuario y un hilo de fondo lee cada Reserva que llegue y la entrega al listener en el EDT.
 * Es independiente del socket de las peticiones (Service).
 */

public class AlertaMonitor implements AutoCloseable {

    private final String servidor;
    private final int puerto;
    private final int idUsuario;
    private final AlertaListener listener;

    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private volatile boolean cerrado;

    public AlertaMonitor(String servidor, int puerto, int idUsuario, AlertaListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("El listener no puede ser nulo.");
        }
        this.servidor = servidor;
        this.puerto = puerto;
        this.idUsuario = idUsuario;
        this.listener = listener;
    }

    /** Conecta y arranca el hilo lector. Lanza IOException si no hay canal de alertas. */
    public void start() throws IOException {
        socket = new Socket(servidor, puerto);
        try {
            os = new ObjectOutputStream(socket.getOutputStream());   // primero el de salida
            os.flush();
            is = new ObjectInputStream(socket.getInputStream());

            os.writeInt(idUsuario);
            os.flush();
            if (is.readInt() != Protocol.ERROR_NO_ERROR) {
                throw new IOException("El servidor rechazó el canal de alertas.");
            }
        } catch (IOException | RuntimeException ex) {
            socket.close();
            throw ex;
        }

        Thread hilo = new Thread(this::leer, "AlertaMonitor");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void leer() {
        while (!cerrado) {
            try {
                Object recibido = is.readObject();
                if (recibido instanceof Reserva reserva) {
                    SwingUtilities.invokeLater(() -> listener.alerta(reserva));
                }
            } catch (IOException | ClassNotFoundException ex) {
                if (!cerrado) {
                    System.err.println("Se perdió el canal de alertas: " + ex.getMessage());
                }
                return;
            }
        }
    }

    @Override
    public void close() {
        cerrado = true;
        try {
            if (os != null) {
                synchronized (os) {
                    os.writeInt(Protocol.DISCONNECT);
                    os.flush();
                }
            }
        } catch (IOException ex) {
            // el servidor ya cerró
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
            // ya estaba cerrado
        }
    }
}