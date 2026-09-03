package sistema.reservas.unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.dao.RecursoDAO;
import sistema.reservas.Categoria.CategoriaRecurso;
import sistema.reservas.Recurso.Recurso;
import sistema.reservas.Recurso.RecursoService;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RecursoServiceTest {
    private RecursoService recursoService;
    private FakeRecursoDAO recursoDAO;

    private CategoriaRecurso categoria;

    @BeforeEach
    void setUp() {
        recursoDAO = new FakeRecursoDAO();
        recursoService = new RecursoService(recursoDAO);

        categoria = new CategoriaRecurso(1, "Laptop Windows", "Laptops con Windows 11");
    }


    // CREAR RECURSO


    @Test
    void crearRecurso_guardaCorrectamente() {
        Recurso recurso = new Recurso("1111", "Laptop #111", "Laptop Dell", categoria);
        recursoService.crearRecurso(recurso);
        assertEquals(recurso, recursoDAO.buscarPorId("1111"));
    }

    @Test
    void crearRecurso_lanzaExcepcionSiIDYaExiste() {
        Recurso recurso1 = new Recurso("2222", "Laptop #2222", "Laptop Dell", categoria);

        Recurso recurso2 = new Recurso("2222", "Laptop #2222", "Otra laptop", categoria);

        recursoService.crearRecurso(recurso1);

        assertThrows(IllegalArgumentException.class, () -> recursoService.crearRecurso(recurso2));
    }


    // VALIDACIONES


    @Test
    void crearRecurso_lanzaExcepcionSiRecursoEsNulo() {
        assertThrows(IllegalArgumentException.class, () -> recursoService.crearRecurso(null));
    }

    @Test
    void crearRecurso_lanzaExcepcionSiIDEsNulo() {
        Recurso recurso = new Recurso(null, "Laptop", "Laptop Windows", categoria);
        assertThrows(IllegalArgumentException.class, () -> recursoService.crearRecurso(recurso));
    }

    @Test
    void crearRecurso_lanzaExcepcionSiIDEstaVacio() {
        Recurso recurso = new Recurso("   ", "Laptop", "Laptop Windows", categoria);

        assertThrows(IllegalArgumentException.class, () -> recursoService.crearRecurso(recurso));
    }

    @Test
    void crearRecurso_lanzaExcepcionSiCategoriaEsNula() {
        Recurso recurso = new Recurso("3333", "Laptop", "Laptop Windows", null);

        assertThrows(IllegalArgumentException.class, () -> recursoService.crearRecurso(recurso));
    }

    @Test
    void crearRecurso_lanzaExcepcionSiDescripcionEsNula() {
        Recurso recurso = new Recurso("4444", "Laptop", null, categoria);

        assertThrows(IllegalArgumentException.class, () -> recursoService.crearRecurso(recurso));
    }

    @Test
    void crearRecurso_lanzaExcepcionSiDescripcionEstaVacia() {
        Recurso recurso = new Recurso("5555", "Laptop", "   ", categoria);

        assertThrows(IllegalArgumentException.class, () -> recursoService.crearRecurso(recurso));
    }


    // BUSCAR


    @Test
    void buscarRecurso_retornaRecursoExistente() {

        Recurso recurso = new Recurso("6666", "Laptop #6666", "Laptop Dell", categoria);

        recursoDAO.guardar(recurso);

        Recurso resultado = recursoService.buscarRecurso("6666");

        assertNotNull(resultado);
        assertEquals("6666", resultado.getId());
    }

    @Test
    void buscarRecurso_lanzaExcepcionSiIdEsNulo() {
        assertThrows(IllegalArgumentException.class, () -> recursoService.buscarRecurso(null));
    }

    @Test
    void buscarRecurso_lanzaExcepcionSiIdEstaVacio() {
        assertThrows(IllegalArgumentException.class, () -> recursoService.buscarRecurso("   "));
    }


    // LISTAR


    @Test
    void listarRecursos_retornaTodosLosRecursos() {

        Recurso recurso1 = new Recurso("1", "Laptop 1", "Laptop", categoria);

        Recurso recurso2 = new Recurso("2", "Laptop 2", "Laptop", categoria);

        recursoDAO.guardar(recurso1);
        recursoDAO.guardar(recurso2);

        List<Recurso> resultado = recursoService.listarRecursos();

        assertEquals(2, resultado.size());
    }

    @Test
    void listarPorCategoria_retornaRecursosDeCategoria() {
        CategoriaRecurso otraCategoria = new CategoriaRecurso(2, "Proyector", "Proyectores");

        Recurso laptop = new Recurso("1", "Laptop", "Laptop", categoria);

        Recurso proyector = new Recurso("2", "Proyector", "Proyector", otraCategoria);

        recursoDAO.guardar(laptop);
        recursoDAO.guardar(proyector);

        List<Recurso> resultado = recursoService.listarPorCategoria(1);

        assertEquals(1, resultado.size());

        assertEquals("1", resultado.get(0).getId());
    }


    // MODIFICAR


    @Test
    void modificarRecurso_actualizaCorrectamente() {

        Recurso recurso = new Recurso("7777", "Laptop", "Descripción original", categoria);

        recursoDAO.guardar(recurso);

        recurso.setDescripcion("Descripción modificada");

        recursoService.modificarRecurso(recurso);

        Recurso actualizado = recursoDAO.buscarPorId("7777");

        assertEquals("Descripción modificada", actualizado.getDescripcion());
    }

    @Test
    void modificarRecurso_lanzaExcepcionSiNoExiste() {

        Recurso recurso = new Recurso("999", "Laptop", "Descripción", categoria);

        assertThrows(IllegalArgumentException.class, () -> recursoService.modificarRecurso(recurso));
    }


    // ELIMINAR


    @Test
    void eliminarRecurso_eliminaCorrectamente() {

        Recurso recurso = new Recurso("8888", "Laptop", "Laptop", categoria);

        recursoDAO.guardar(recurso);

        recursoService.eliminarRecurso("8888");

        assertNull(recursoDAO.buscarPorId("8888"));
    }

    @Test
    void eliminarRecurso_lanzaExcepcionSiNoExiste() {

        assertThrows(IllegalArgumentException.class, () -> recursoService.eliminarRecurso("1"));
    }


    // FAKE DAO


    private static class FakeRecursoDAO implements RecursoDAO {
        private final List<Recurso> recursos = new ArrayList<>();

        @Override
        public void guardar(Recurso recurso) {
            recursos.add(recurso);
        }

        @Override
        public Recurso buscarPorId(String id) {
            for (Recurso recurso : recursos) {
                if (recurso.getId().equals(id)) {
                    return recurso;
                }
            }
            return null;
        }

        @Override
        public List<Recurso> listar() {
            return new ArrayList<>(recursos);
        }

        @Override
        public List<Recurso> listarPorCategoria(int idCategoria) {
            List<Recurso> resultado = new ArrayList<>();
            for (Recurso recurso : recursos) {
                if (recurso.getCategoria().getId() == idCategoria) {
                    resultado.add(recurso);
                }
            }
            return resultado;
        }

        @Override
        public void actualizar(Recurso recurso) {

            for (int i = 0;
                 i < recursos.size();
                 i++) {

                if (recursos.get(i)
                        .getId()
                        .equals(recurso.getId())) {

                    recursos.set(i, recurso);
                    return;
                }
            }
        }

        @Override
        public void eliminar(String id) {
            recursos.removeIf(recurso -> recurso.getId().equals(id));
        }
    }
}