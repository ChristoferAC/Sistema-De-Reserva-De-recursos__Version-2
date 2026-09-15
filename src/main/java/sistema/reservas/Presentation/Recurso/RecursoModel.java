package sistema.reservas.Presentation.Recurso;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Presentation.AbstractModel;

import java.util.ArrayList;
import java.util.List;

public class RecursoModel extends AbstractModel {

    public static final String CURRENT = "current";
    public static final String RECURSOS = "recursos";
    public static final String CATEGORIAS = "categorias";

    private Recurso current;
    private List<Recurso> recursos;
    private List<CategoriaRecurso> categorias;

    public RecursoModel() {
        current = null;
        recursos = new ArrayList<>();
        categorias = new ArrayList<>();
    }

    public Recurso getCurrent() {
        return current;
    }

    public void setCurrent(Recurso current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
        firePropertyChange(RECURSOS);
    }

    public List<CategoriaRecurso> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaRecurso> categorias) {
        this.categorias = categorias;
        firePropertyChange(CATEGORIAS);
    }
}