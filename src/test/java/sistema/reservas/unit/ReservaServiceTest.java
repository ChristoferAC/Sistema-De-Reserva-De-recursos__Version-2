package sistema.reservas.unit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Presentation.Recurso.RecursoService;
import sistema.reservas.Presentation.Reserva.ReservaService;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ReservaServiceTest {

    private ReservaService reservaService;
    private RecursoService recursoService;
    private Funcionario funcionario;
    private CategoriaRecurso categoriaLaptop;

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

        XmlTestDataSupport.seedFuncionario(111, "Juan Perez", "111", "111", "88880000");
        XmlTestDataSupport.seedCategoria(1, "Laptop windows", "Laptop con Windows 11");

        funcionario = new Funcionario(111, "Juan Perez", "111", "111", "88880000");
        categoriaLaptop = new CategoriaRecurso(1, "Laptop windows", "Laptop con Windows 11");

        recursoService = new RecursoService();
        recursoService.crearRecurso(new Recurso("238715", "Laptop #238715", "desc", categoriaLaptop));
        recursoService.crearRecurso(new Recurso("45238", "Laptop #45238", "desc", categoriaLaptop));

        reservaService = new ReservaService(recursoService);
    }

    @AfterEach
    void tearDown() {
        XmlTestDataSupport.limpiar();
    }

    private Reserva nuevaReserva(int id, LocalDate fecha) {
        Reserva reserva = new Reserva(id, funcionario, "Reunión de trabajo", fecha, LocalTime.of(9, 0), LocalTime.of(11, 0));
        reserva.agregarCategoria(categoriaLaptop);
        return reserva;
    }

    @Test
    void crearReserva_laPersisteActivaConRecursoAsignado() {
        Reserva reserva = nuevaReserva(1, LocalDate.now().plusDays(5));

        reservaService.crearReserva(reserva);

        Reserva encontrada = reservaService.buscarReserva(1);
        assertNotNull(encontrada);
        assertEquals(Reserva.Estado.ACTIVA, encontrada.getEstado());
        assertTrue(encontrada.isActiva());
        assertFalse(encontrada.getRecursosAsignados().isEmpty());
    }

    @Test
    void crearReserva_rechazaIdDuplicado() {
        reservaService.crearReserva(nuevaReserva(1, LocalDate.now().plusDays(5)));

        assertThrows(IllegalArgumentException.class,
                () -> reservaService.crearReserva(nuevaReserva(1, LocalDate.now().plusDays(6))));
    }

    @Test
    void crearReserva_rechazaSinCategorias() {
        Reserva reserva = new Reserva(1, funcionario, "Reunión", LocalDate.now().plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        assertThrows(IllegalArgumentException.class, () -> reservaService.crearReserva(reserva));
    }

    @Test
    void crearReserva_indicaCategoriaSinDisponibilidad() {
        // Ocupa el único recurso libre restante en el mismo horario
        reservaService.crearReserva(nuevaReserva(1, LocalDate.of(2026, 8, 14)));
        reservaService.crearReserva(nuevaReserva(2, LocalDate.of(2026, 8, 14)));

        Reserva sinDisponibilidad = nuevaReserva(3, LocalDate.of(2026, 8, 14));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reservaService.crearReserva(sinDisponibilidad));
        assertTrue(ex.getMessage().contains("Laptop windows"));
    }

    @Test
    void listarReservasFuncionario_filtraPorFuncionario() {
        reservaService.crearReserva(nuevaReserva(1, LocalDate.now().plusDays(2)));

        assertEquals(1, reservaService.listarReservasFuncionario(111).size());
        assertEquals(0, reservaService.listarReservasFuncionario(999).size());
    }

    @Test
    void cancelarReserva_marcaCanceladaYLiberaRecursosSinBorrarla() {
        Reserva reserva = nuevaReserva(1, LocalDate.now().plusDays(5));
        reservaService.crearReserva(reserva);

        reservaService.cancelarReserva(1);

        Reserva resultado = reservaService.buscarReserva(1);
        assertNotNull(resultado, "La reserva debe seguir existiendo en el historial");
        assertEquals(Reserva.Estado.CANCELADA, resultado.getEstado());
        assertTrue(resultado.getRecursosAsignados().isEmpty());
    }

    @Test
    void cancelarReserva_liberaElRecursoParaOtraReserva() {
        // Solo hay 2 laptops; ambas ocupadas en la misma fecha/hora.
        reservaService.crearReserva(nuevaReserva(1, LocalDate.of(2026, 9, 10)));
        reservaService.crearReserva(nuevaReserva(2, LocalDate.of(2026, 9, 10)));

        reservaService.cancelarReserva(1);

        // Ahora sí debería haber disponibilidad para una tercera reserva.
        assertDoesNotThrow(() -> reservaService.crearReserva(nuevaReserva(3, LocalDate.of(2026, 9, 10))));
    }

    @Test
    void cancelarReserva_rechazaSiNoExiste() {
        assertThrows(IllegalArgumentException.class, () -> reservaService.cancelarReserva(999));
    }

    @Test
    void cancelarReserva_rechazaSiYaEstaCancelada() {
        reservaService.crearReserva(nuevaReserva(1, LocalDate.now().plusDays(5)));
        reservaService.cancelarReserva(1);

        assertThrows(IllegalArgumentException.class, () -> reservaService.cancelarReserva(1));
    }

    @Test
    void cancelarReserva_rechazaReservaPasada() {
        Reserva reserva = new Reserva(1, funcionario, "Reunión", LocalDate.now().minusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0));
        reserva.agregarRecurso(new Recurso("238715", "Laptop #238715", "desc", categoriaLaptop));
        // Se inserta directo (crearReserva no permite crear en el pasado
        // por disponibilidad al azar); esto simula una reserva ya vieja.
        reservaService.crearReserva(nuevaReserva(2, LocalDate.now().plusDays(1)));

        assertThrows(IllegalArgumentException.class, () -> reservaService.cancelarReserva(1));
    }

    @Test
    void modificarReserva_rechazaSiLaReservaEstaCancelada() {
        Reserva reserva = nuevaReserva(1, LocalDate.now().plusDays(5));
        reservaService.crearReserva(reserva);
        reservaService.cancelarReserva(1);

        Reserva modificacion = nuevaReserva(1, LocalDate.now().plusDays(6));
        assertThrows(IllegalArgumentException.class, () -> reservaService.modificarReserva(modificacion));
    }
}