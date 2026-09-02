package sistema.reservas.Recurso;

import sistema.reservas.Categoria.CategoriaRecurso;

public class Recurso {
    private String id;
    private String nombre;
    private String descripcion;
    private CategoriaRecurso categoria;

    public Recurso(String id,String nombre,String descripcion, CategoriaRecurso categoria){
        this.id=id;
        this.nombre= nombre;
        this.descripcion= descripcion;
        this.categoria=categoria;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public CategoriaRecurso getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaRecurso categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "Recurso{" + "id='" + id + '\'' + ", categoria=" + categoria + ", descripcion='" + descripcion + '\'' + '}';
    }
}
