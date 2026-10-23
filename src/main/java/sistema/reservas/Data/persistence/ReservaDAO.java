package sistema.reservas.Data.persistence;

import sistema.reservas.Logic.Reserva;

import java.time.LocalDate;
import java.util.List;

public interface ReservaDAO {
    Reserva insertar(Reserva reserva);              // transaccional; deja el id generado en la reserva
    Reserva buscarPorId(int id);
    List<Reserva> listar();
    List<Reserva> listarPorFuncionario(int idFuncionario);
    List<Reserva> listarPorFecha(LocalDate fecha);  // todas; el Service filtra las ACTIVA
    void actualizar(Reserva reserva);
}