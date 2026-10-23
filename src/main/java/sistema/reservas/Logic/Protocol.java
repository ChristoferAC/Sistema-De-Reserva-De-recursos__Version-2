// ============================================================
// PROTOCOL / src/main/java/sistema/reservas/Logic/Protocol.java
// Si I3 ya publicó Protocol.java, pega solo el bloque "I2".
// ============================================================
package sistema.reservas.Logic;

public class Protocol {

    // ---------- Generales (acordados con I3, igual que el profe) ----------
    public static final String SERVER = "localhost";
    public static final int PORT = 1234;
    public static final int PORT_ALERTAS = 1235;   // provisional, canal asincrónico de alertas

    public static final int ERROR_NO_ERROR = 0;
    public static final int ERROR_ERROR = 1;

    public static final int DISCONNECT = 99;       // bloque I3: Cierre / stop()

    // ---------- I1: 101-199 Usuarios y login | 201-299 Funcionarios | 301-399 Categorías ----------
    // (I1 agrega aquí sus constantes: USUARIO_LOGIN, FUNCIONARIO_CREATE = 201, ...)

    // ---------- I2: 401-499 Recursos ----------
    public static final int RECURSO_CREATE = 401;
    public static final int RECURSO_READ = 402;
    public static final int RECURSO_UPDATE = 403;
    public static final int RECURSO_DELETE = 404;
    public static final int RECURSO_FIND_ALL = 405;
    public static final int RECURSO_FIND_BY_CATEGORIA = 406;

    // ---------- I2: 501-599 Reservas ----------
    public static final int RESERVA_CREATE = 501;
    public static final int RESERVA_UPDATE = 502;
    public static final int RESERVA_CANCELAR = 503;
    public static final int RESERVA_FIND_ALL = 504;
    public static final int RESERVA_FIND_BY_FUNCIONARIO = 505;
    public static final int RESERVA_EXTRAER_IA = 506;
}