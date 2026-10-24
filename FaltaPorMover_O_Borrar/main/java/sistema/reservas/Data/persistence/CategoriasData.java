package sistema.reservas.Data.persistence;

import sistema.reservas.Logic.CategoriaRecurso;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Envoltorio raiz para serializar/des-serializar la lista completa de
 * Categorias de Recurso a un solo XML.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoriasData {

    @XmlElementWrapper(name = "categorias")
    @XmlElement(name = "categoria")
    private List<CategoriaRecurso> categorias = new ArrayList<>();

    public List<CategoriaRecurso> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaRecurso> categorias) {
        this.categorias = categorias;
    }
}
