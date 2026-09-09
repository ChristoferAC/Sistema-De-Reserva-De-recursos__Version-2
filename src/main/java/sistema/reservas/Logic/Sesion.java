package sistema.reservas.Logic;

/**
 * Guarda el Usuario que hizo login, para que cualquier parte de la
 * aplicacion sepa quien esta usando el sistema y con que rol
 * (Administrador o Funcionario), sin tener que pasarlo como parametro
 * por todos lados.
 */
public class Sesion {

    private static Usuario usuario;

    public static Usuario getUsuario() {
        return usuario;
    }

    public static void setUsuario(Usuario usuario) {
        Sesion.usuario = usuario;
    }

    public static void logout() {
        Sesion.usuario = null;
    }

    public static boolean isLoggedIn() {
        return usuario != null;
    }
}
