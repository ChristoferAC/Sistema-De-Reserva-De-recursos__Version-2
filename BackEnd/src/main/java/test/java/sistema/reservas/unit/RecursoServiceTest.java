// ============================================================
// BACKEND / src/test/java/sistema/reservas/unit/RecursoServiceTest.java
// Reemplaza al test viejo (XML + XmlTestDataSupport). Prueba ServiceRecurso con un
// RecursoDAO en memoria: no toca archivos ni MySQL. El DAO falso imita los mensajes
// y las excepciones del RecursoDAOJdbc real.
// ============================================================
package test.java.sistema.reservas.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.Data.persistence.RecursoDAO;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Services.ServiceRecurso;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RecursoServiceTest {

    private FakeRecursoDAO dao;
    private ServiceRecurso service;
    private CategoriaRecurso categoriaLaptop;
    private CategoriaRecurso categoriaSala;

    @BeforeEach
    void setUp() {
        categoriaLaptop = new CategoriaRecurso(1, "Laptop windows", "Laptop con Windows 11");
        categoriaSala = new CategoriaRecurso(2, "Sala de Juntas", "Sala para 10 personas");

        dao = new FakeRecursoDAO();
        service = new ServiceRecurso(dao, () -> List.of(categoriaLaptop, categoriaSala));
    }

    @Test
    void crearRecurso_loPersisteYPuedeConsultarse() {
        service.crearRecurso(new Recurso("238715", "Laptop #238715", "Laptop con Windows 11", categoriaLaptop));

        Recurso encontrado = service.buscarRecurso("238715");
        assertNotNull(encontrado);
        assertEquals("Laptop #238715", encontrado.getNombre());
        assertEquals(1, encontrado.getCategoria().getId());
    }

    @Test
    void crearRecurso_rechazaIdDuplicado() {
        service.crearRecurso(new Recurso("238715", "Laptop #238715", "desc", categoriaLaptop));

        Recurso duplicado = new Recurso("238715", "Otra laptop", "otra desc", categoriaLaptop);
        assertThrows(IllegalArgumentException.class, () -> service.crearRecurso(duplicado));
    }

    @Test
    void crearRecurso_rechazaSinCategoria() {
        Recurso recurso = new Recurso("1", "Nombre", "Descripcion", null);
        assertThrows(IllegalArgumentException.class, () -> service.crearRecurso(recurso));
    }

    @Test
    void crearRecurso_rechazaIdVacio() {
        Recurso recurso = new Recurso("", "Nombre", "Descripcion", categoriaLaptop);
        assertThrows(IllegalArgumentException.class, () -> service.crearRecurso(recurso));
    }

    @Test
    void crearRecurso_recortaEspaciosDelId() {
        service.crearRecurso(new Recurso("  777  ", " Proyector ", " con cable ", categoriaSala));

        assertNotNull(service.buscarRecurso("777"));
        assertEquals("Proyector", service.buscarRecurso("777").getNombre());
    }

    @Test
    void buscarRecurso_devuelveNullSiNoExiste() {
        assertNull(service.buscarRecurso("no-existe"));
    }

    @Test
    void buscarRecurso_rechazaIdVacio() {
        assertThrows(IllegalArgumentException.class, () -> service.buscarRecurso("  "));
    }

    @Test
    void listarPorCategoria_filtraCorrectamente() {
        service.crearRecurso(new Recurso("238715", "Laptop #238715", "desc", categoriaLaptop));
        service.crearRecurso(new Recurso("34343", "Sala 1", "desc", categoriaSala));

        List<Recurso> laptops = service.listarPorCategoria(1);

        assertEquals(1, laptops.size());
        assertEquals("238715", laptops.get(0).getId());
    }

    @Test
    void modificarRecurso_actualizaDatos() {
        service.crearRecurso(new Recurso("238715", "Laptop #238715", "desc", categoriaLaptop));

        service.modificarRecurso(new Recurso("238715", "Laptop actualizada", "desc nueva", categoriaLaptop));

        assertEquals("Laptop actualizada", service.buscarRecurso("238715").getNombre());
    }

    @Test
    void modificarRecurso_rechazaSiNoExiste() {
        Recurso recurso = new Recurso("no-existe", "Nombre", "Descripcion", categoriaLaptop);
        assertThrows(IllegalArgumentException.class, () -> service.modificarRecurso(recurso));
    }

    @Test
    void eliminarRecurso_loQuitaDeLaLista() {
        service.crearRecurso(new Recurso("238715", "Laptop #238715", "desc", categoriaLaptop));

        service.eliminarRecurso("238715");

        assertNull(service.buscarRecurso("238715"));
    }

    @Test
    void eliminarRecurso_rechazaSiNoExiste() {
        assertThrows(IllegalArgumentException.class, () -> service.eliminarRecurso("no-existe"));
    }

    @Test
    void eliminarRecurso_conReservasPropagaElMensajeDelDao() {
        service.crearRecurso(new Recurso("238715", "Laptop #238715", "desc", categoriaLaptop));
        dao.idsConReservas.add("238715");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.eliminarRecurso("238715"));

        assertEquals("No se puede eliminar el recurso: tiene reservas asociadas.", ex.getMessage());
        assertNotNull(service.buscarRecurso("238715"));   // no se borró
    }

    @Test
    void listarCategorias_usaElProveedorDeI1() {
        assertEquals(2, service.listarCategorias().size());
    }

    @Test
    void constructor_rechazaDependenciasNulas() {
        assertThrows(IllegalArgumentException.class, () -> new ServiceRecurso(null, ArrayList::new));
        assertThrows(IllegalArgumentException.class, () -> new ServiceRecurso(dao, null));
    }

    // ------------------------------------------------------------------

    /** DAO en memoria que imita a RecursoDAOJdbc (ids de texto, mismos mensajes de error). */
    static class FakeRecursoDAO implements RecursoDAO {

        private final Map<String, Recurso> datos = new LinkedHashMap<>();
        final List<String> idsConReservas = new ArrayList<>();

        @Override
        public void insertar(Recurso recurso) {
            if (datos.containsKey(recurso.getId())) {
                throw new IllegalArgumentException("Ya existe un recurso con el ID indicado.");
            }
            datos.put(recurso.getId(), recurso);
        }

        @Override
        public Recurso buscarPorId(String id) {
            return datos.get(id);
        }

        @Override
        public List<Recurso> listar() {
            return new ArrayList<>(datos.values());
        }

        @Override
        public List<Recurso> listarPorCategoria(int idCategoria) {
            List<Recurso> resultado = new ArrayList<>();
            for (Recurso recurso : datos.values()) {
                if (recurso.getCategoria().getId() == idCategoria) {
                    resultado.add(recurso);
                }
            }
            return resultado;
        }

        @Override
        public void actualizar(Recurso recurso) {
            if (!datos.containsKey(recurso.getId())) {
                throw new IllegalArgumentException("El recurso no existe.");
            }
            datos.put(recurso.getId(), recurso);
        }

        @Override
        public void eliminar(String id) {
            if (!datos.containsKey(id)) {
                throw new IllegalArgumentException("El recurso no existe.");
            }
            if (idsConReservas.contains(id)) {
                throw new IllegalArgumentException("No se puede eliminar el recurso: tiene reservas asociadas.");
            }
            datos.remove(id);
        }
    }
}