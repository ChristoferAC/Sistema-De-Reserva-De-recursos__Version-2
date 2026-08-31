package sistema.reservas.unit;

import sistema.reservas.dao.CategoriaRecursoDAO;
import sistema.reservas.model.CategoriaRecurso;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO falso en memoria para pruebas. Simula la misma regla de id
 * autogenerado (máximo + 1) que CategoriaRecursoDAOXml, para que las
 * pruebas de CategoriaRecursoService reflejen el comportamiento real.
 */
public class CategoriaRecursoDAOFalso implements CategoriaRecursoDAO {

    private final List<CategoriaRecurso> categorias = new ArrayList<>();

    @Override
    public CategoriaRecurso buscarPorId(int id) {
        for (CategoriaRecurso c : categorias) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    @Override
    public List<CategoriaRecurso> buscarPorDescripcion(String descripcion) {
        List<CategoriaRecurso> resultado = new ArrayList<>();
        for (CategoriaRecurso c : categorias) {
            if (c.getDescripcion() != null
                    && c.getDescripcion().toLowerCase().contains(descripcion.toLowerCase())) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    @Override
    public void guardar(CategoriaRecurso categoria) {
        categoria.setId(siguienteId());
        categorias.add(categoria);
    }

    @Override
    public void actualizar(CategoriaRecurso categoria) {
        for (int i = 0; i < categorias.size(); i++) {
            if (categorias.get(i).getId() == categoria.getId()) {
                categorias.set(i, categoria);
                return;
            }
        }
    }

    @Override
    public void eliminar(int id) {
        categorias.removeIf(c -> c.getId() == id);
    }

    @Override
    public List<CategoriaRecurso> listarTodos() {
        return new ArrayList<>(categorias);
    }

    private int siguienteId() {
        int maximo = 0;
        for (CategoriaRecurso c : categorias) {
            if (c.getId() > maximo) {
                maximo = c.getId();
            }
        }
        return maximo + 1;
    }
}