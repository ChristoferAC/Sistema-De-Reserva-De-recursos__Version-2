package sistema.reservas.Presentation.Categoria;

import sistema.reservas.Data.persistence.CategoriaXmlPersister;
import sistema.reservas.Data.persistence.CategoriasData;
import sistema.reservas.Logic.CategoriaRecurso;

import java.util.List;

/**
 * Las categorias se guardan en data/categorias.xml, leido/escrito con
 * JAXB via CategoriaXmlPersister. Se cargan una sola vez y se guardan
 * de nuevo cada vez que algo cambia.
 */
public class CategoriaRecursoService {

    private static final CategoriaXmlPersister persister = new CategoriaXmlPersister();
    private static CategoriasData data;
    private static int siguienteId = 1;

    /** Usado solo en pruebas: limpia la cache en memoria para que el siguiente
     *  acceso vuelva a leer el XML desde disco. */
    public static void resetParaPruebas() {
        data = null;
        siguienteId = 1;
    }

    private static CategoriasData data() {
        if (data == null) {
            try {
                data = persister.load();
            } catch (Exception e) {
                throw new RuntimeException("No se pudo cargar data/categorias.xml", e);
            }
            for (CategoriaRecurso c : data.getCategorias()) {
                if (c.getId() >= siguienteId) {
                    siguienteId = c.getId() + 1;
                }
            }
        }
        return data;
    }

    private static void guardar() {
        try {
            persister.store(data);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar data/categorias.xml", e);
        }
    }

    public CategoriaRecurso buscarPorId(int id) {
        for (CategoriaRecurso c : data().getCategorias()) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public List<CategoriaRecurso> buscarPorDescripcion(String descripcion) {
        List<CategoriaRecurso> resultado = new java.util.ArrayList<>();
        for (CategoriaRecurso c : data().getCategorias()) {
            if (c.getDescripcion().toLowerCase().contains(descripcion.toLowerCase())) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public List<CategoriaRecurso> listarTodas() {
        return data().getCategorias();
    }

    public void crear(CategoriaRecurso categoria) {
        validar(categoria);
        // El id lo asigna este Service (antes lo iba a asignar el DAO al persistir).
        categoria.setId(siguienteId++);
        data().getCategorias().add(categoria);
        guardar();
    }

    public void actualizar(CategoriaRecurso categoria) {
        validar(categoria);
        if (buscarPorId(categoria.getId()) == null) {
            throw new IllegalArgumentException("No existe una categoría con ese ID.");
        }
        guardar();
    }

    public void eliminar(int id) {
        CategoriaRecurso existente = buscarPorId(id);
        if (existente == null) {
            throw new IllegalArgumentException("No existe una categoría con ese ID.");
        }
        data().getCategorias().remove(existente);
        guardar();
    }

    private void validar(CategoriaRecurso categoria) {
        if (categoria.getDescripcion() == null || categoria.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
    }
}