// ============================================================
// PROTOCOL / src/main/java/sistema/reservas/Logic/Administrador.java
// ============================================================
package sistema.reservas.Logic;

public class Administrador extends Usuario {

    private static final long serialVersionUID = 1L;

    public Administrador() {
        super();
    }

    public Administrador(int id, String nombre, String username, String password) {
        super(id, nombre, username, password);
    }

    @Override
    public String getRol() {
        return "ADMIN";
    }
}