package sistema.reservas.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.dao.RecursoDAO;
import sistema.reservas.dao.ReservaDAO;
import sistema.reservas.Categoria.CategoriaRecurso;
import sistema.reservas.Funcionario.Funcionario;
import sistema.reservas.Recurso.Recurso;
import sistema.reservas.Reserva.Reserva;
import sistema.reservas.Reserva.ReservaService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaServiceTest {

    private FakeRecursoDAO recursoDAO;
    private FakeReservaDAO reservaDAO;

    private ReservaService reservaService;

    private CategoriaRecurso categoriaLaptop;
    private CategoriaRecurso categoriaProyector;

    private Recurso laptop1;
    private Recurso laptop2;
    private Recurso proyector1;

    private Funcionario funcionario;

    private final LocalDate fecha = LocalDate.of(2026, 9, 10);

    @BeforeEach
    void setUp() {

        recursoDAO = new FakeRecursoDAO();

        reservaDAO = new FakeReservaDAO();

        reservaService = new ReservaService(reservaDAO, recursoDAO);

        categoriaLaptop = new CategoriaRecurso(1, "Laptop Windows 11", "Laptop Windows 11");

        categoriaProyector = new CategoriaRecurso(2, "Proyector", "Proyector HD");

        laptop1 = new Recurso("L001", "Laptop 1", "Laptop Dell", categoriaLaptop);

        laptop2 = new Recurso("L002", "Laptop 2", "Laptop Dell", categoriaLaptop);

        proyector1 = new Recurso("P001", "Proyector 1", "Proyector Epson", categoriaProyector);

        recursoDAO.guardar(laptop1);
        recursoDAO.guardar(laptop2);
        recursoDAO.guardar(proyector1);

        funcionario = new Funcionario(111, "Juan Perez",
                "111", "111","1");
    }


    // CREAR RESERVA


    @Test
    void crearReserva_asignaPrimerRecursoDisponible() {

        Reserva reserva = crearReservaBase(2);

        reserva.agregarCategoria(categoriaLaptop);

        reservaService.crearReserva(reserva);

        assertEquals(1, reserva.getRecursosAsignados().size());

        assertEquals("L001", reserva.getRecursosAsignados().get(0).getId());
    }

    @Test
    void crearReserva_asignaPrimerRecursoDisponibleDeCadaCategoria() {

        Reserva reserva = crearReservaBase(1);

        reserva.agregarCategoria(categoriaLaptop);

        reserva.agregarCategoria(categoriaProyector);

        reservaService.crearReserva(reserva);

        assertEquals(2, reserva.getRecursosAsignados().size());

        assertEquals("L001", reserva.getRecursosAsignados().get(0).getId());

        assertEquals("P001",
                reserva.getRecursosAsignados().get(1).getId());
    }

    @Test
    void crearReserva_noPermiteIDDuplicado() {

        Reserva reserva1 = crearReservaBase(1);

        reserva1.agregarCategoria(categoriaLaptop);

        reservaService.crearReserva(reserva1);

        Reserva reserva2 = crearReservaBase(1);

        reserva2.agregarCategoria(categoriaLaptop);

        assertThrows(IllegalArgumentException.class, () -> reservaService.crearReserva(reserva2));
    }


    // DISPONIBILIDAD


    @Test
    void hayDisponibilidad_retornaTrueCuandoExisteRecursoDisponible() {

        Reserva reserva = crearReservaBase(1);

        reserva.agregarCategoria(categoriaLaptop);

        assertTrue(reservaService.hayDisponibilidad(reserva));
    }

    @Test
    void hayDisponibilidad_retornaFalseCuandoTodosLosRecursosEstanOcupados() {

        Reserva reservaExistente = crearReservaBase(1);

        reservaExistente.agregarRecurso(laptop1);

        reservaExistente.agregarRecurso(laptop2);

        reservaDAO.guardar(reservaExistente);

        Reserva nuevaReserva = crearReservaBase(2);

        nuevaReserva.agregarCategoria(categoriaLaptop);

        assertFalse(reservaService.hayDisponibilidad(nuevaReserva));
    }

    @Test
    void obtenerCategoriasSinDisponibilidad_identificaCategoria() {

        Reserva reservaExistente = crearReservaBase(1);

        reservaExistente.agregarRecurso(proyector1);

        reservaDAO.guardar(reservaExistente);

        Reserva nuevaReserva = crearReservaBase(2);

        nuevaReserva.agregarCategoria(categoriaProyector);

        List<CategoriaRecurso> resultado = reservaService.obtenerCategoriasSinDisponibilidad(nuevaReserva,-1);

        assertEquals(1, resultado.size());

        assertEquals(categoriaProyector, resultado.get(0));
    }


    // SOLAPAMIENTO


    // =========================================================
// PRUEBA 1: DOS RESERVAS SE SOLAPAN Y NO HAY OTRO RECURSO
// =========================================================

    @Test
    void crearReserva_rechazaSolapamiento() {

        // La primera reserva ocupa L001
        Reserva reservaExistente = new Reserva(1, funcionario, "Reunión", fecha, LocalTime.of(10, 0), LocalTime.of(12, 0));

        reservaExistente.agregarRecurso(laptop1);

        reservaDAO.guardar(reservaExistente);

        // Ocupamos también L002 para que no exista
        // otro recurso disponible de la misma categoría.
        Reserva otraReserva = new Reserva(2, funcionario, "Capacitación", fecha, LocalTime.of(10, 0), LocalTime.of(12, 0));

        otraReserva.agregarRecurso(laptop2);

        reservaDAO.guardar(otraReserva);

        // Nueva reserva en horario solapado
        Reserva nuevaReserva = new Reserva(3, funcionario, "Clase", fecha, LocalTime.of(11, 0), LocalTime.of(13, 0));

        nuevaReserva.agregarCategoria(categoriaLaptop);

        assertFalse(reservaService.hayDisponibilidad(nuevaReserva));

        assertThrows(IllegalArgumentException.class, () -> reservaService.crearReserva(nuevaReserva));
    }


// =========================================================
// PRUEBA 2: DOS RESERVAS CONSECUTIVAS SÍ SON VÁLIDAS
// =========================================================

    @Test
    void crearReserva_permiteReservasSinSolapamiento() {

        // Primera reserva:
        // 10:00 - 12:00
        Reserva reservaExistente = new Reserva(1, funcionario, "Reunión", fecha, LocalTime.of(10, 0), LocalTime.of(12, 0));

        // La primera reserva utiliza L001
        reservaExistente.agregarRecurso(laptop1);

        reservaDAO.guardar(reservaExistente);

        // Nueva reserva:
        // 12:00 - 14:00
        Reserva nuevaReserva = new Reserva(2, funcionario, "Capacitación", fecha, LocalTime.of(13, 0), LocalTime.of(14, 0));

        nuevaReserva.agregarCategoria(categoriaLaptop);

        // Debe existir disponibilidad porque
        // la primera reserva termina exactamente
        // cuando comienza la segunda.
        assertTrue(reservaService.hayDisponibilidad(nuevaReserva));

        // Se crea la reserva
        reservaService.crearReserva(nuevaReserva);

        // Verificamos que se haya asignado un recurso
        assertFalse(nuevaReserva.getRecursosAsignados().isEmpty());

        // L001 debe ser el primero disponible
        assertEquals("L001", nuevaReserva.getRecursosAsignados().get(0).getId());
    }


    // VARIAS CATEGORÍAS


    @Test
    void crearReserva_rechazaSiUnaCategoriaNoTieneDisponibilidad() {

        Reserva reservaExistente = crearReservaBase(1);

        reservaExistente.agregarRecurso(proyector1);

        reservaDAO.guardar(reservaExistente);

        Reserva nuevaReserva = crearReservaBase(2);

        nuevaReserva.agregarCategoria(categoriaLaptop);

        nuevaReserva.agregarCategoria(categoriaProyector);

        assertThrows(IllegalArgumentException.class, () -> reservaService.crearReserva(nuevaReserva));
    }


    // VALIDACIONES


    @Test
    void crearReserva_rechazaReservaNula() {
        assertThrows(IllegalArgumentException.class, () -> reservaService.crearReserva(null));
    }

    @Test
    void crearReserva_rechazaFuncionarioNulo() {

        Reserva reserva = new Reserva(1, null, "Reunión", fecha, LocalTime.of(10, 0), LocalTime.of(12, 0));

        reserva.agregarCategoria(categoriaLaptop);

        assertThrows(IllegalArgumentException.class, () -> reservaService.crearReserva(reserva
                )
        );
    }

    @Test
    void crearReserva_rechazaActividadVacia() {

        Reserva reserva = new Reserva(1, funcionario, "   ", fecha, LocalTime.of(10, 0), LocalTime.of(12, 0));

        reserva.agregarCategoria(categoriaLaptop);

        assertThrows(IllegalArgumentException.class, () -> reservaService.crearReserva(reserva));
    }

    @Test
    void crearReserva_rechazaHoraInicioPosteriorAHoraFin() {

        Reserva reserva = new Reserva(1, funcionario, "Reunión", fecha, LocalTime.of(14, 0), LocalTime.of(12, 0));

        reserva.agregarCategoria(categoriaLaptop);

        assertThrows(IllegalArgumentException.class, () -> reservaService.crearReserva(reserva));
    }

    @Test
    void crearReserva_rechazaSiNoHayCategorias() {

        Reserva reserva = crearReservaBase(1);

        assertThrows(IllegalArgumentException.class, () -> reservaService.crearReserva(reserva));
    }


    // MODIFICAR


    @Test
    void modificarReserva_actualizaReserva() {

        Reserva reserva = crearReservaBase(1);

        reserva.agregarCategoria(categoriaLaptop);

        reservaService.crearReserva(reserva);

        reserva.setActividad("Actividad modificada");

        reservaService.modificarReserva(reserva);

        Reserva resultado = reservaDAO.buscarPorId(1);

        assertEquals("Actividad modificada", resultado.getActividad());
    }

    @Test
    void modificarReserva_lanzaExcepcionSiNoExiste() {

        Reserva reserva = crearReservaBase(999);

        reserva.agregarCategoria(categoriaLaptop);

        assertThrows(IllegalArgumentException.class, () -> reservaService.modificarReserva(reserva));
    }


    // CANCELAR


    @Test
    void cancelarReserva_eliminaReserva() {

        Reserva reserva = crearReservaBase(1);

        reserva.agregarCategoria(categoriaLaptop);

        reservaService.crearReserva(reserva);

        assertNotNull(reservaDAO.buscarPorId(1));

        reservaService.cancelarReserva(1);

        assertNull(reservaDAO.buscarPorId(1));
    }

    @Test
    void cancelarReserva_lanzaExcepcionSiNoExiste() {

        assertThrows(IllegalArgumentException.class, () -> reservaService.cancelarReserva(999));
    }


    // CREAR RESERVA DE PRUEBA


    private Reserva crearReservaBase(int id) {

        return new Reserva(id, funcionario, "Reunión de proyecto", fecha, LocalTime.of(10, 0), LocalTime.of(12, 0));
    }


    // FAKE RECURSO DAO


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
        public List<Recurso>
        listarPorCategoria(int idCategoria) {
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
            for (int i = 0; i < recursos.size(); i++) {

                if (recursos.get(i).getId().equals(recurso.getId())) {
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


    // FAKE RESERVA DAO


    private static class FakeReservaDAO implements ReservaDAO {

        private final List<Reserva> reservas = new ArrayList<>();

        @Override
        public void guardar(Reserva reserva) {
            reservas.add(reserva);
        }

        @Override
        public Reserva buscarPorId(int id) {
            for (Reserva reserva : reservas) {
                if (reserva.getId() == id) {
                    return reserva;
                }
            }
            return null;
        }

        @Override
        public List<Reserva> listar() {
            return new ArrayList<>(reservas);
        }

        @Override
        public List<Reserva>
        listarPorFuncionario(int idFuncionario) {
            List<Reserva> resultado = new ArrayList<>();
            for (Reserva reserva : reservas) {
                if (reserva.getFuncionario() != null && reserva.getFuncionario().getId() == idFuncionario) {
                    resultado.add(reserva);
                }
            }
            return resultado;
        }

        @Override
        public List<Reserva> listarPorFecha(String fecha) {
            List<Reserva> resultado = new ArrayList<>();
            for (Reserva reserva : reservas) {
                if (reserva.getFecha() != null && reserva.getFecha().toString().equals(fecha)) {resultado.add(reserva);
                }
            }
            return resultado;
        }

        @Override
        public void actualizar(Reserva reserva) {
            for (int i = 0; i < reservas.size(); i++) {
                if (reservas.get(i).getId() == reserva.getId()) {
                    reservas.set(i, reserva);
                    return;
                }
            }
        }

        @Override
        public void eliminar(int id) {
            reservas.removeIf(reserva -> reserva.getId() == id);
        }
    }
}