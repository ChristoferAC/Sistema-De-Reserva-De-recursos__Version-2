package sistema.reservas.unit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Presentation.Recurso.RecursoService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecursoServiceTest {

    private RecursoService recursoService;
    private CategoriaRecurso categoriaLaptop;
    private CategoriaRecurso categoriaSala;

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
        XmlTestDataSupport.seedCategoria(1, "Laptop windows", "Laptop con Windows 11");
        XmlTestDataSupport.seedCategoria(2, "Sala de Juntas", "Sala para 10 personas");

        categoriaLaptop = new CategoriaRecurso(1, "Laptop windows", "Laptop con Windows 11");
        categoriaSala = new CategoriaRecurso(2, "Sala de Juntas", "Sala para 10 personas");

        recursoService = new RecursoService();
    }

    @AfterEach
    void tearDown() {
        XmlTestDataSupport.limpiar();
    }

    @Test
    void crearRecurso_loPersisteYPuedeConsultarse() {
        Recurso recurso = new Recurso("238715", "Laptop #238715", "Laptop con Windows 11", categoriaLaptop);

        recursoService.crearRecurso(recurso);

        Recurso encontrado = recursoService.buscarRecurso("238715");
        assertNotNull(encontrado);
        assertEquals("Laptop #238715", encontrado.getNombre());
        assertEquals(1, encontrado.getCategoria().getId());
    }

    @Test
    void crearRecurso_rechazaIdDuplicado() {
        recursoService.crearRecurso(new Recurso("238715", "Laptop #238715", "desc", categoriaLaptop));

        Recurso duplicado = new Recurso("238715", "Otra laptop", "otra desc", categoriaLaptop);
        assertThrows(IllegalArgumentException.class, () -> recursoService.crearRecurso(duplicado));
    }

    @Test
    void crearRecurso_rechazaSinCategoria() {
        Recurso recurso = new Recurso("1", "Nombre", "Descripcion", null);
        assertThrows(IllegalArgumentException.class, () -> recursoService.crearRecurso(recurso));
    }

    @Test
    void crearRecurso_rechazaIdVacio() {
        Recurso recurso = new Recurso("", "Nombre", "Descripcion", categoriaLaptop);
        assertThrows(IllegalArgumentException.class, () -> recursoService.crearRecurso(recurso));
    }

    @Test
    void listarPorCategoria_filtraCorrectamente() {
        recursoService.crearRecurso(new Recurso("238715", "Laptop #238715", "desc", categoriaLaptop));
        recursoService.crearRecurso(new Recurso("34343", "Sala 1", "desc", categoriaSala));

        List<Recurso> laptops = recursoService.listarPorCategoria(1);

        assertEquals(1, laptops.size());
        assertEquals("238715", laptops.get(0).getId());
    }

    @Test
    void modificarRecurso_actualizaDatos() {
        recursoService.crearRecurso(new Recurso("238715", "Laptop #238715", "desc", categoriaLaptop));

        Recurso modificado = new Recurso("238715", "Laptop actualizada", "desc nueva", categoriaLaptop);
        recursoService.modificarRecurso(modificado);

        assertEquals("Laptop actualizada", recursoService.buscarRecurso("238715").getNombre());
    }

    @Test
    void modificarRecurso_rechazaSiNoExiste() {
        Recurso recurso = new Recurso("no-existe", "Nombre", "Descripcion", categoriaLaptop);
        assertThrows(IllegalArgumentException.class, () -> recursoService.modificarRecurso(recurso));
    }

    @Test
    void eliminarRecurso_loQuitaDeLaLista() {
        recursoService.crearRecurso(new Recurso("238715", "Laptop #238715", "desc", categoriaLaptop));

        recursoService.eliminarRecurso("238715");

        assertNull(recursoService.buscarRecurso("238715"));
    }

    @Test
    void eliminarRecurso_rechazaSiNoExiste() {
        assertThrows(IllegalArgumentException.class, () -> recursoService.eliminarRecurso("no-existe"));
    }
}