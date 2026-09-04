package sistema.reservas.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.Logic.Funcionario.Funcionario;
import sistema.reservas.Logic.Funcionario.FuncionarioService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FuncionarioServiceTest {

    private FuncionarioDAOFalso funcionarioDAO;
    private FuncionarioService funcionarioService;

    @BeforeEach
    void setUp() {
        funcionarioDAO = new FuncionarioDAOFalso();
        funcionarioService = new FuncionarioService(funcionarioDAO);
    }

    @Test
    void crearAsignaLaClaveInicialIgualAlId() {
        Funcionario nuevo = new Funcionario(111, "Juan Perez", "111", "loQueSea", "8888-0000");

        funcionarioService.crear(nuevo);

        assertEquals("111", funcionarioDAO.buscarPorId(111).getPassword());
    }

    @Test
    void crearConIdDuplicadoLanzaExcepcion() {
        funcionarioService.crear(new Funcionario(111, "Juan Perez", "111", "x", "8888-0000"));
        Funcionario duplicado = new Funcionario(111, "Otro Nombre", "111b", "y", "8888-1111");

        assertThrows(IllegalArgumentException.class, () -> funcionarioService.crear(duplicado));
    }

    @Test
    void crearSinNombreLanzaExcepcion() {
        Funcionario sinNombre = new Funcionario(222, "", "222", "x", "8888-2222");

        assertThrows(IllegalArgumentException.class, () -> funcionarioService.crear(sinNombre));
    }

    @Test
    void crearSinTelefonoLanzaExcepcion() {
        Funcionario sinTelefono = new Funcionario(222, "Maria Perez", "222", "x", " ");

        assertThrows(IllegalArgumentException.class, () -> funcionarioService.crear(sinTelefono));
    }

    @Test
    void actualizarFuncionarioInexistenteLanzaExcepcion() {
        Funcionario noExiste = new Funcionario(999, "Nadie", "999", "x", "0000-0000");

        assertThrows(IllegalArgumentException.class, () -> funcionarioService.actualizar(noExiste));
    }

    @Test
    void eliminarFuncionarioInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> funcionarioService.eliminar(999));
    }

    @Test
    void buscarPorNombreEncuentraCoincidenciasParciales() {
        funcionarioService.crear(new Funcionario(111, "Juan Perez", "111", "x", "8888-0000"));
        funcionarioService.crear(new Funcionario(222, "Maria Perez", "222", "x", "8888-1111"));

        List<Funcionario> resultado = funcionarioService.buscarPorNombre("perez");

        assertEquals(2, resultado.size());
    }
}