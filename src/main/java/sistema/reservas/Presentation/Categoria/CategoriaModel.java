package sistema.reservas.Presentation.Categoria;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Presentation.AbstractModel;

import java.util.ArrayList;
import java.util.List;

public class CategoriaModel extends AbstractModel {

    public static final String CURRENT = "current";
    public static final String CATEGORIAS = "categorias";

    private CategoriaRecurso current;
    private List<CategoriaRecurso> categorias;

    public CategoriaModel() {
        current = null;
        categorias = new ArrayList<>();
    }

    public CategoriaRecurso getCurrent() {
        return current;
    }

    public void setCurrent(CategoriaRecurso current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public List<CategoriaRecurso> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaRecurso> categorias) {
        this.categorias = categorias;
        firePropertyChange(CATEGORIAS);
    }
}
