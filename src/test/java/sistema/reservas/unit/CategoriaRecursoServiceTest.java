package sistema.reservas.unit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Presentation.Categoria.CategoriaRecursoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoriaRecursoServiceTest {

    private CategoriaRecursoService categoriaService;

    @BeforeAll
    static void respaldarDatosReales() {
        XmlTestDataSupport.respaldar();
    }

    @AfterAll
    static void restaurarDatosReales() {
        XmlTestDataSupport.restaurar();
    }

    @BeforeEach
    void setUp() {
        XmlTestDataSupport.limpiar();
        CategoriaRecursoService.resetParaPruebas();
        categoriaService = new CategoriaRecursoService();
    }

    @AfterEach
    void tearDown() {
        XmlTestDataSupport.limpiar();
        CategoriaRecursoService.resetParaPruebas();
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

    @Test
    void categoriaPersisteRealmenteEnElArchivoXml() {
        categoriaService.crear(new CategoriaRecurso(0, "", "Sala de Juntas"));

        // Simula "reiniciar la aplicacion": se limpia el cache en memoria
        // y se vuelve a leer desde el archivo data/categorias.xml.
        CategoriaRecursoService.resetParaPruebas();
        CategoriaRecursoService otraInstancia = new CategoriaRecursoService();

        assertEquals(1, otraInstancia.listarTodas().size());
    }
}
