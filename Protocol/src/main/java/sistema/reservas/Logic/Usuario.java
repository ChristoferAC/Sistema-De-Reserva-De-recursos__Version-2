// ============================================================
// PROTOCOL / src/main/java/sistema/reservas/Logic/Usuario.java
// Sin JAXB; Serializable. El password lo devuelve el Backend siempre en null (regla 5).
// ============================================================
package sistema.reservas.Logic;

import java.io.Serializable;

public abstract class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String username;
    private String password;

    protected Usuario() {
    }

    public Usuario(int id, String nombre, String username, String password) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.password = password;
    }

    public abstract String getRol();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}