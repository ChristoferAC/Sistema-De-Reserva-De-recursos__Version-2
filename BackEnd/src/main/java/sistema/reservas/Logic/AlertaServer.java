package sistema.reservas.Logic;

import sistema.reservas.Logic.Services.Notificador;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Canal asincrónico de alertas (segundo ServerSocket, en Protocol.PORT_ALERTAS).
 *
 * Cada Frontend abre aquí una conexión después de su login y le manda su id de usuario (int);
 * el servidor responde ERROR_NO_ERROR y desde ese momento le empuja cada Reserva nueva que
 * crean los demás. El creador de la reserva NO la recibe.
 *
 * Un cliente lento o caído no afecta a los demás: cada uno tiene su propia cola de envío, y
 * si falla se quita de la lista. Implementa Notificador para enchufarse a ServiceReserva.
 */
public class AlertaServer implements Notificador {

    private static final Logger LOG = Logger.getLogger(AlertaServer.class.getName());

    private static final class Cliente {
        final int idUsuario;
        final Socket socket;
        final ObjectOutputStream os;
        final ExecutorService cola = Executors.newSingleThreadExecutor(r -> {
            Thread hilo = new Thread(r, "Alerta-envio-" + r.hashCode());
            hilo.setDaemon(true);
            return hilo;
        });

        Cliente(int idUsuario, Socket socket, ObjectOutputStream os) {
            this.idUsuario = idUsuario;
            this.socket = socket;
            this.os = os;
        }

        void cerrar() {
            cola.shutdownNow();
            try {
                socket.close();
            } catch (IOException ex) {
                // ya estaba cerrado
            }
        }
    }

    private final int puerto;
    private final List<Cliente> clientes = new CopyOnWriteArrayList<>();
    private ServerSocket serverSocket;
    private volatile boolean activo;

    public AlertaServer(int puerto) {
        this.puerto = puerto;
    }

    public synchronized void start() throws IOException {
        if (activo) {
            return;
        }
        serverSocket = new ServerSocket(puerto);   // lanza IOException si el puerto está ocupado
        activo = true;
        new Thread(this::aceptar, "AlertaServer-aceptar").start();
        System.out.println("Canal de alertas escuchando en el puerto " + serverSocket.getLocalPort());
    }

    private void aceptar() {
        while (activo) {
            try {
                Socket socket = serverSocket.accept();
                new Thread(() -> atender(socket), "AlertaServer-cliente").start();
            } catch (IOException ex) {
                if (activo) {
                    LOG.log(Level.WARNING, "Error al aceptar un cliente de alertas", ex);
                }
            }
        }
    }

    /** Registra al cliente y se queda leyendo solo para enterarse cuando se desconecta. */
    private void atender(Socket socket) {
        Cliente cliente = null;
        try {
            // Mismo orden que en todo el proyecto: primero el de salida, luego el de entrada.
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            os.flush();
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

            int idUsuario = is.readInt();
            cliente = new Cliente(idUsuario, socket, os);
            synchronized (os) {   // el aviso de "registrado" sale antes que cualquier alerta
                clientes.add(cliente);
                os.writeInt(Protocol.ERROR_NO_ERROR);
                os.flush();
            }
            System.out.println("Alertas: cliente " + idUsuario + " conectado. Activos: " + clientes.size());

            while (true) {
                if (is.readInt() == Protocol.DISCONNECT) {
                    break;
                }
            }
        } catch (EOFException | SocketException ex) {
            // el cliente se fue sin avisar o el servidor cerró el socket
        } catch (IOException ex) {
            LOG.log(Level.FINE, "Conexión de alertas terminada: " + ex.getMessage());
        } finally {
            if (cliente != null) {
                quitar(cliente);
            } else {
                try {
                    socket.close();
                } catch (IOException ex) {
                    // ya estaba cerrado
                }
            }
        }
    }

    private void quitar(Cliente cliente) {
        if (clientes.remove(cliente)) {
            cliente.cerrar();
            System.out.println("Alertas: cliente " + cliente.idUsuario + " desconectado. Activos: " + clientes.size());
        }
    }

    /** Lo llama ServiceReserva después del commit. El origen se identifica por el funcionario de la reserva. */
    @Override
    public void nuevaReserva(Reserva reserva, Object origen) {
        if (reserva == null) {
            return;
        }
        int idOrigen = reserva.getFuncionario() != null ? reserva.getFuncionario().getId() : -1;
        notificar(reserva, idOrigen);
    }

    /** Envía la reserva a todos los clientes activos menos a los del usuario que la creó. */
    public void notificar(Reserva reserva, int idOrigen) {
        for (Cliente cliente : clientes) {
            if (cliente.idUsuario == idOrigen) {
                continue;
            }
            try {
                cliente.cola.execute(() -> enviar(cliente, reserva));
            } catch (RuntimeException ex) {
                quitar(cliente);   // la cola ya estaba cerrada
            }
        }
    }

    private void enviar(Cliente cliente, Reserva reserva) {
        try {
            synchronized (cliente.os) {
                cliente.os.reset();   // sin reset, el stream reenvía como referencia una reserva ya enviada
                cliente.os.writeObject(reserva);
                cliente.os.flush();
            }
        } catch (IOException ex) {
            quitar(cliente);          // un cliente caído no debe tumbar el aviso a los demás
        }
    }

    public int clientesConectados() {
        return clientes.size();
    }

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
        for (Cliente cliente : clientes) {
            quitar(cliente);
        }
    }
}