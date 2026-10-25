package sistema.reservas.Logic;

import java.util.List;

/**
 * Todo lo que un Worker necesita durante la vida de su cliente: su conexion a MySQL, los
 * Services construidos con esa conexion y los Handlers que los exponen.
 * Se cierra cuando el cliente se desconecta (aqui se cierra la conexion JDBC).
 */
public interface ContextoCliente extends AutoCloseable {

    List<Handler> handlers();

    @Override
    void close();
}