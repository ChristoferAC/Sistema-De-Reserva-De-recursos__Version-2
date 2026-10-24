// ============================================================
// FRONTEND / src/main/java/sistema/reservas/Logic/Service.java
// Proxy (implements IService). Compila contra el IService actual (stop + bloque I2).
// Si I3 ya tiene su Service.java, conserva su constructor/instance()/stop() y pega solo
// leerEstado() y los métodos marcados "I2". I1 agrega sus métodos donde se indica.
//
// Reglas del protocolo que cumple (regla 3):
//  - ObjectOutputStream se crea ANTES que ObjectInputStream (si no, ambos lados se cuelgan).
//  - Cada petición: writeInt(código) + writeObject(argumento) + flush().
//  - Respuesta: int ERROR_NO_ERROR [+ resultado]  |  int ERROR_ERROR + String mensaje.
//  - os.reset() antes de cada petición: sin él, ObjectOutputStream reenvía como referencia
//    un objeto ya enviado y el Backend recibiría el estado viejo de una Reserva/Recurso
//    que el Controller modificó y volvió a mandar.
//  - Métodos synchronized: un solo hilo a la vez usa el socket (EDT + hilos de fondo).
// ============================================================
package sistema.reservas.Logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class Service implements IService {
    private static Service theInstance;

    public static synchronized IService instance() {
        if (theInstance == null) {
            theInstance = new Service();
        }
        return theInstance;
    }

    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;

    private Service() {
        try {
            socket = new Socket(Protocol.SERVER, Protocol.PORT);
            os = new ObjectOutputStream(socket.getOutputStream());   // primero el de salida
            os.flush();
            is = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No se pudo conectar con el servidor (" + Protocol.SERVER + ":" + Protocol.PORT + "): "
                            + ex.getMessage(), ex);
        }
    }

    /** Lee el estado de la respuesta; si fue error, lanza el mensaje real que escribió el Worker. */
    private void leerEstado() throws Exception {
        if (is.readInt() == Protocol.ERROR_ERROR) {
            throw new Exception((String) is.readObject());
        }
    }

    // ---------- I3: 99 Cierre ----------
    @Override
    public synchronized void stop() {
        try {
            os.reset();
            os.writeInt(Protocol.DISCONNECT);
            os.flush();
            socket.shutdownOutput();
            socket.close();
        } catch (IOException ex) {
            // ya estaba cerrado: no hay nada más que hacer
        }
    }

    // ---------- I1: 101-399 Usuarios, Funcionarios, Categorías ----------
    // (I1 agrega aquí sus métodos del Proxy)

    // ---------- I2: 401-499 Recursos ----------
    @Override
    public synchronized void create(Recurso recurso) throws Exception {
        os.reset();
        os.writeInt(Protocol.RECURSO_CREATE);
        os.writeObject(recurso);
        os.flush();
        leerEstado();
    }

    @Override
    public synchronized Recurso read(Recurso recurso) throws Exception {
        os.reset();
        os.writeInt(Protocol.RECURSO_READ);
        os.writeObject(recurso);
        os.flush();
        leerEstado();
        return (Recurso) is.readObject();
    }

    @Override
    public synchronized void update(Recurso recurso) throws Exception {
        os.reset();
        os.writeInt(Protocol.RECURSO_UPDATE);
        os.writeObject(recurso);
        os.flush();
        leerEstado();
    }

    @Override
    public synchronized void delete(Recurso recurso) throws Exception {
        os.reset();
        os.writeInt(Protocol.RECURSO_DELETE);
        os.writeObject(recurso);
        os.flush();
        leerEstado();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized List<Recurso> findAll(Recurso filtro) throws Exception {
        os.reset();
        os.writeInt(Protocol.RECURSO_FIND_ALL);
        os.writeObject(filtro);
        os.flush();
        leerEstado();
        return (List<Recurso>) is.readObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized List<Recurso> findRecursosByCategoria(CategoriaRecurso categoria) throws Exception {
        os.reset();
        os.writeInt(Protocol.RECURSO_FIND_BY_CATEGORIA);
        os.writeObject(categoria);
        os.flush();
        leerEstado();
        return (List<Recurso>) is.readObject();
    }

    // ---------- I2: 501-599 Reservas ----------
    @Override
    public synchronized Reserva create(Reserva reserva) throws Exception {
        os.reset();
        os.writeInt(Protocol.RESERVA_CREATE);
        os.writeObject(reserva);
        os.flush();
        leerEstado();
        return (Reserva) is.readObject();
    }

    @Override
    public synchronized Reserva update(Reserva reserva) throws Exception {
        os.reset();
        os.writeInt(Protocol.RESERVA_UPDATE);
        os.writeObject(reserva);
        os.flush();
        leerEstado();
        return (Reserva) is.readObject();
    }

    @Override
    public synchronized void cancelar(Reserva reserva) throws Exception {
        os.reset();
        os.writeInt(Protocol.RESERVA_CANCELAR);
        os.writeObject(reserva);
        os.flush();
        leerEstado();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized List<Reserva> findAll(Reserva filtro) throws Exception {
        os.reset();
        os.writeInt(Protocol.RESERVA_FIND_ALL);
        os.writeObject(filtro);
        os.flush();
        leerEstado();
        return (List<Reserva>) is.readObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized List<Reserva> findReservasByFuncionario(Funcionario funcionario) throws Exception {
        os.reset();
        os.writeInt(Protocol.RESERVA_FIND_BY_FUNCIONARIO);
        os.writeObject(funcionario);
        os.flush();
        leerEstado();
        return (List<Reserva>) is.readObject();
    }

    @Override
    public synchronized ReservaExtraccion extraerConIA(String frase) throws Exception {
        os.reset();
        os.writeInt(Protocol.RESERVA_EXTRAER_IA);
        os.writeObject(frase);
        os.flush();
        leerEstado();
        return (ReservaExtraccion) is.readObject();
    }






}
