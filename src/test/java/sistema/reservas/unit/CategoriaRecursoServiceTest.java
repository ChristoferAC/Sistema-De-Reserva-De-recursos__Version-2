package sistema.reservas.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.Categoria.CategoriaRecurso;
import sistema.reservas.Categoria.CategoriaRecursoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoriaRecursoServiceTest {

    private CategoriaRecursoDAOFalso categoriaDAO;
    private CategoriaRecursoService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaDAO = new CategoriaRecursoDAOFalso();
        categoriaService = new CategoriaRecursoService(categoriaDAO);
    }

    @Test
    void crearAsignaUnIdAutogenerado() {
        CategoriaRecurso categoria = new CategoriaRecurso(0, "", "Sala para 10 personas");

        categoriaService.crear(categoria);

        assertEquals(1, categoria.getId());
    }

    @Test
    void crearVariasCategoriasAsignaIdsConsecutivos() {
        categoriaService.crear(new CategoriaRecurso(0, "", "Sala para 10 personas"));
        CategoriaRecurso segunda = new CategoriaRecurso(0, "", "Laptop Windows 11");

        categoriaService.crear(segunda);

        assertEquals(2, segunda.getId());
    }

    @Test
    void crearSinDescripcionLanzaExcepcion() {
        CategoriaRecurso sinDescripcion = new CategoriaRecurso(0, "", " ");

        assertThrows(IllegalArgumentException.class, () -> categoriaService.crear(sinDescripcion));
    }

    @Test
    void actualizarCategoriaInexistenteLanzaExcepcion() {
        CategoriaRecurso noExiste = new CategoriaRecurso(999, "", "No existe");

        assertThrows(IllegalArgumentException.class, () -> categoriaService.actualizar(noExiste));
    }

    @Test
    void eliminarCategoriaInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> categoriaService.eliminar(999));
    }

    @Test
    void buscarPorDescripcionEncuentraCoincidenciasParciales() {
        categoriaService.crear(new CategoriaRecurso(0, "", "Sala para 10 personas"));
        categoriaService.crear(new CategoriaRecurso(0, "", "Sala de Juntas"));
        categoriaService.crear(new CategoriaRecurso(0, "", "Laptop Windows 11"));

        List<CategoriaRecurso> resultado = categoriaService.buscarPorDescripcion("sala");

        assertEquals(2, resultado.size());
    }
}