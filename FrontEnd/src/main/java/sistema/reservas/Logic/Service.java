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

    // ---------- I1: 101-199 Usuarios ----------
    @Override
    public synchronized Usuario login(String username, String password) throws Exception {
        os.reset();
        os.writeInt(Protocol.USUARIO_LOGIN);
        os.writeObject(username);
        os.writeObject(password);
        os.flush();
        leerEstado();
        return (Usuario) is.readObject();
    }

    @Override
    public synchronized void cambiarClave(String username, String claveActual, String claveNueva) throws Exception {
        os.reset();
        os.writeInt(Protocol.USUARIO_CAMBIAR_CLAVE);
        os.writeObject(username);
        os.writeObject(claveActual);
        os.writeObject(claveNueva);
        os.flush();
        leerEstado();
    }

    // ---------- I1: 201-299 Funcionarios ----------
    @Override
    public synchronized void create(Funcionario funcionario) throws Exception {
        os.reset();
        os.writeInt(Protocol.FUNCIONARIO_CREATE);
        os.writeObject(funcionario);
        os.flush();
        leerEstado();
    }

    @Override
    public synchronized Funcionario read(Funcionario funcionario) throws Exception {
        os.reset();
        os.writeInt(Protocol.FUNCIONARIO_READ);
        os.writeObject(funcionario);
        os.flush();
        leerEstado();
        return (Funcionario) is.readObject();
    }

    @Override
    public synchronized void update(Funcionario funcionario) throws Exception {
        os.reset();
        os.writeInt(Protocol.FUNCIONARIO_UPDATE);
        os.writeObject(funcionario);
        os.flush();
        leerEstado();
    }

    @Override
    public synchronized void delete(Funcionario funcionario) throws Exception {
        os.reset();
        os.writeInt(Protocol.FUNCIONARIO_DELETE);
        os.writeObject(funcionario);
        os.flush();
        leerEstado();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized List<Funcionario> findAll(Funcionario filtro) throws Exception {
        os.reset();
        os.writeInt(Protocol.FUNCIONARIO_FIND_ALL);
        os.writeObject(filtro);
        os.flush();
        leerEstado();
        return (List<Funcionario>) is.readObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized List<Funcionario> findFuncionariosByNombre(String nombre) throws Exception {
        os.reset();
        os.writeInt(Protocol.FUNCIONARIO_FIND_BY_NOMBRE);
        os.writeObject(nombre);
        os.flush();
        leerEstado();
        return (List<Funcionario>) is.readObject();
    }

    // ---------- I1: 301-399 Categorías ----------
    @Override
    public synchronized CategoriaRecurso create(CategoriaRecurso categoria) throws Exception {
        os.reset();
        os.writeInt(Protocol.CATEGORIA_CREATE);
        os.writeObject(categoria);
        os.flush();
        leerEstado();
        return (CategoriaRecurso) is.readObject();
    }

    @Override
    public synchronized CategoriaRecurso read(CategoriaRecurso categoria) throws Exception {
        os.reset();
        os.writeInt(Protocol.CATEGORIA_READ);
        os.writeObject(categoria);
        os.flush();
        leerEstado();
        return (CategoriaRecurso) is.readObject();
    }

    @Override
    public synchronized void update(CategoriaRecurso categoria) throws Exception {
        os.reset();
        os.writeInt(Protocol.CATEGORIA_UPDATE);
        os.writeObject(categoria);
        os.flush();
        leerEstado();
    }

    @Override
    public synchronized void delete(CategoriaRecurso categoria) throws Exception {
        os.reset();
        os.writeInt(Protocol.CATEGORIA_DELETE);
        os.writeObject(categoria);
        os.flush();
        leerEstado();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized List<CategoriaRecurso> findAll(CategoriaRecurso filtro) throws Exception {
        os.reset();
        os.writeInt(Protocol.CATEGORIA_FIND_ALL);
        os.writeObject(filtro);
        os.flush();
        leerEstado();
        return (List<CategoriaRecurso>) is.readObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized List<CategoriaRecurso> findCategoriasByDescripcion(String descripcion) throws Exception {
        os.reset();
        os.writeInt(Protocol.CATEGORIA_FIND_BY_DESCRIPCION);
        os.writeObject(descripcion);
        os.flush();
        leerEstado();
        return (List<CategoriaRecurso>) is.readObject();
    }

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