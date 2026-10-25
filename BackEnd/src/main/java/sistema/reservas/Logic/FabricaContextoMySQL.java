package sistema.reservas.Logic;

import sistema.reservas.Data.persistence.DataBase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Fabrica REAL de contextos (Integrante 3): cada Worker nuevo recibe SU propia conexion a
 * MySQL, y los Handlers de cada modulo se arman con esa conexion.
 *
 * Es el UNICO lugar donde I1 e I2 enchufan sus Handlers (ver construirHandlers).
 */
public class FabricaContextoMySQL implements FabricaContexto {

    @Override
    public ContextoCliente crear(Worker worker) throws Exception {
        final Connection conexion = DataBase.instance().getConnection();
        final List<Handler> handlers;
        try {
            handlers = construirHandlers(conexion, worker);
        } catch (Exception ex) {
            conexion.close();   // si falla el armado, no dejar la conexion abierta
            throw ex;
        }
        return new ContextoCliente() {
            @Override
            public List<Handler> handlers() {
                return handlers;
            }

            @Override
            public void close() {
                try {
                    conexion.close();
                } catch (SQLException ex) {
                    System.err.println("No se pudo cerrar la conexion: " + ex.getMessage());
                }
            }
        };
    }

    /**
     * Aqui se agregan los handlers de cada modulo, una linea por integrante:
     *     handlers.add(new UsuariosCategoriasHandler(conexion, worker));   // I1 (pendiente)
     *     handlers.add(new RecursosReservasHandler(conexion, worker));     // I2 (pendiente)
     * 'worker' sirve de "origen" para el Notificador (para no avisarle al que creo la reserva).
     */
    private List<Handler> construirHandlers(Connection conexion, Worker worker) throws Exception {
        List<Handler> handlers = new ArrayList<>();
        return handlers;
    }
}