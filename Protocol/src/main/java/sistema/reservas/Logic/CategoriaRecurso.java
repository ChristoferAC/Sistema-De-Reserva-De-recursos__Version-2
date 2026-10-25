// ============================================================
// PROTOCOL / src/main/java/sistema/reservas/Logic/CategoriaRecurso.java
// ============================================================
package sistema.reservas.Logic;

import java.io.Serializable;

public class CategoriaRecurso implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String descripcion;

    public CategoriaRecurso() {
    }

    public CategoriaRecurso(int id, String nombre, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}