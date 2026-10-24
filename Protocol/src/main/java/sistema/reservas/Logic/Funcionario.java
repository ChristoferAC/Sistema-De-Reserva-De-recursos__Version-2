// ============================================================
// PROTOCOL / src/main/java/sistema/reservas/Logic/Funcionario.java
// ============================================================
package sistema.reservas.Logic;

public class Funcionario extends Usuario {

    private static final long serialVersionUID = 1L;

    private String telefono;

    public Funcionario() {
        super();
    }

    public Funcionario(int id, String nombre, String username, String password, String telefono) {
        super(id, nombre, username, password);
        this.telefono = telefono;
    }

    @Override
    public String getRol() {
        return "FUNCIONARIO";
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}