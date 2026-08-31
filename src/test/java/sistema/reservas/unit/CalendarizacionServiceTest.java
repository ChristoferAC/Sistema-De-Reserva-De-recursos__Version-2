package sistema.reservas.unit;

import org.junit.jupiter.api.Test;
import sistema.reservas.dao.RecursoDAO;
import sistema.reservas.dao.ReservaDAO;
import sistema.reservas.model.CategoriaRecurso;
import sistema.reservas.model.Funcionario;
import sistema.reservas.model.Recurso;
import sistema.reservas.model.Reserva;
import sistema.reservas.service.CalendarizacionService;
import sistema.reservas.service.MatrizCalendarizacion;
import sistema.reservas.service.RecursoService;
import sistema.reservas.service.ReservaService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarizacionServiceTest {

    @Test
    void generarMatriz_marcaCeldaOcupadaCuandoHayReservaEnEsaHora() {
        CategoriaRecurso categoriaLaptop = new CategoriaRecurso(1, "Laptop windows", "Laptop windows 11");
        Recurso laptop1 = new Recurso("238715", "Laptop #238715", "Laptop windows 11", categoriaLaptop);
        Recurso laptop2 = new Recurso("45238", "Laptop #45238", "Laptop windows 11", categoriaLaptop);
        List<Recurso> recursos = List.of(laptop1, laptop2);

        Funcionario juan = new Funcionario(111, "Juan Perez", "111", "111", "0000-0000");
        Reserva reserva = new Reserva(1, juan, "Sesion de Junta Directiva",
                LocalDate.of(2026, 8, 5), LocalTime.of(9, 0), LocalTime.of(11, 0));
        reserva.agregarRecurso(laptop1);
        List<Reserva> reservas = List.of(reserva);

        RecursoDAO recursoDAOFake = fakeRecursoDAO(recursos);
        ReservaDAO reservaDAOFake = fakeReservaDAO(reservas);

        ReservaService reservaService = new ReservaService(reservaDAOFake, recursoDAOFake);
        RecursoService recursoService = new RecursoService(recursoDAOFake);
        CalendarizacionService calendarizacionService = new CalendarizacionService(reservaService, recursoService);

        MatrizCalendarizacion matriz = calendarizacionService.generarMatriz(
                LocalDate.of(2026, 8, 5), categoriaLaptop);

        int filaNueveAM = matriz.getHoras().indexOf(LocalTime.of(9, 0));
        int columnaLaptop1 = matriz.getRecursos().indexOf(laptop1);
        int columnaLaptop2 = matriz.getRecursos().indexOf(laptop2);

        String celdaOcupada = matriz.getCelda(filaNueveAM, columnaLaptop1);
        String celdaLibre = matriz.getCelda(filaNueveAM, columnaLaptop2);

        assertTrue(celdaOcupada.contains("Sesion de Junta Directiva"));
        assertTrue(celdaOcupada.contains("Juan Perez"));
        assertTrue(celdaLibre.isEmpty());
    }

    @Test
    void generarMatriz_lanzaExcepcionSiFechaEsNula() {
        RecursoDAO recursoDAOFake = fakeRecursoDAO(List.of());
        ReservaDAO reservaDAOFake = fakeReservaDAO(List.of());

        CalendarizacionService service = new CalendarizacionService(
                new ReservaService(reservaDAOFake, recursoDAOFake),
                new RecursoService(recursoDAOFake));

        CategoriaRecurso categoria = new CategoriaRecurso(1, "Sala de Juntas", "Sala de Juntas");

        assertThrows(IllegalArgumentException.class, () -> service.generarMatriz(null, categoria));
    }

    @Test
    void generarMatriz_lanzaExcepcionSiCategoriaEsNula() {
        RecursoDAO recursoDAOFake = fakeRecursoDAO(List.of());
        ReservaDAO reservaDAOFake = fakeReservaDAO(List.of());

        CalendarizacionService service = new CalendarizacionService(
                new ReservaService(reservaDAOFake, recursoDAOFake),
                new RecursoService(recursoDAOFake));

        assertThrows(IllegalArgumentException.class,
                () -> service.generarMatriz(LocalDate.of(2026, 8, 5), null));
    }

    private RecursoDAO fakeRecursoDAO(List<Recurso> recursos) {
        return new RecursoDAO() {
            @Override public void guardar(Recurso recurso) {}
            @Override public Recurso buscarPorId(String id) { return null; }
            @Override public List<Recurso> listar() { return recursos; }
            @Override public List<Recurso> listarPorCategoria(int idCategoria) { return recursos; }
            @Override public void actualizar(Recurso recurso) {}
            @Override public void eliminar(String id) {}
        };
    }

    private ReservaDAO fakeReservaDAO(List<Reserva> reservas) {
        return new ReservaDAO() {
            @Override public void guardar(Reserva reserva) {}
            @Override public Reserva buscarPorId(int id) { return null; }
            @Override public List<Reserva> listar() { return reservas; }
            @Override public List<Reserva> listarPorFuncionario(int idFuncionario) { return List.of(); }
            @Override public List<Reserva> listarPorFecha(String fecha) { return List.of(); }
            @Override public void actualizar(Reserva reserva) {}
            @Override public void eliminar(int id) {}
        };
    }
}
