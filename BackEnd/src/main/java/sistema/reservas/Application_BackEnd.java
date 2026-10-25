package sistema.reservas;

import sistema.reservas.Data.persistence.DataBase;
import sistema.reservas.Logic.Protocol;
import sistema.reservas.Logic.Server;
import sistema.reservas.Logic.FabricaContextoMySQL;

import java.sql.Connection;
import java.util.Scanner;

/**
 * Main del Backend (Integrante 3). Sin interfaz grafica.
 * Uso:  java -jar BackEnd.jar [puerto]      (si no se indica, usa Protocol.PORT)
 */
public class Application_BackEnd {

    public static void main(String[] args) {
        int puerto = Protocol.PORT;
        if (args.length > 0) {
            try {
                puerto = Integer.parseInt(args[0].trim());
            } catch (NumberFormatException ex) {
                System.err.println("Puerto invalido: '" + args[0] + "'. Uso: java -jar BackEnd.jar [puerto]");
                return;
            }
        }

        // 1) Probar MySQL antes de abrir el puerto: si falla, mejor enterarse de una vez.
        try (Connection prueba = DataBase.instance().getConnection()) {
            System.out.println("Conexion a MySQL correcta.");
        } catch (Exception ex) {
            System.err.println("No se pudo conectar a MySQL: " + ex.getMessage());
            System.err.println("Revise database.properties y que MySQL este encendido y con el script reservas.sql cargado.");
            return;
        }

        // 2) Abrir el servidor.
        Server server = new Server(puerto, new FabricaContextoMySQL());
        try {
            server.start();
        } catch (Exception ex) {
            System.err.println("No se pudo abrir el puerto " + puerto + " (¿ya hay otro servidor corriendo?): " + ex.getMessage());
            return;
        }

        // 3) Detener con ENTER para cerrar bien los sockets.
        System.out.println("Presione ENTER para detener el servidor.");
        new Scanner(System.in).nextLine();
        server.stop();
        System.out.println("Servidor detenido.");
    }
}