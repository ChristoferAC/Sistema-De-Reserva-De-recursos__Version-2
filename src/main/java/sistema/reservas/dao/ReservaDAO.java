package sistema.reservas.dao;
import sistema.reservas.Logic.Reserva.Reserva;
import java.util.List;

public interface ReservaDAO {

    void guardar(Reserva reserva);

    Reserva buscarPorId(int id);

    List<Reserva> listar();

    List<Reserva> listarPorFuncionario(int idFuncionario);

    List<Reserva> listarPorFecha(String fecha);

    void actualizar(Reserva reserva);

    void eliminar(int id);
}