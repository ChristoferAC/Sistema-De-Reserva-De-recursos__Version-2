package sistema.reservas.Logic;

/**
 * Crea el ContextoCliente de cada Worker nuevo. La version real (FabricaContextoMySQL) abre la
 * conexion con Database.instance().getConnection() y arma los Services de I1 e I2; en las
 * pruebas se usa una version falsa, sin MySQL.
 */
@FunctionalInterface
public interface FabricaContexto {

    ContextoCliente crear(Worker worker) throws Exception;
}