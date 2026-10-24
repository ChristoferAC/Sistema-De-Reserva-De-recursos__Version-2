package sistema.reservas.Logic;

import sistema.reservas.Logic.Reserva;
import sistema.reservas.Logic.ReservaExtraccion;

import java.util.List;

public interface IServiceReserva {
    Reserva crearReserva(Reserva reserva);
    Reserva buscarReserva(int id);                 // null si no existe
    List<Reserva> listarReservas();
    List<Reserva> listarReservasFuncionario(int idFuncionario);
    Reserva modificarReserva(Reserva reserva);
    void cancelarReserva(int id);                  // estado CANCELADA, no borra
    boolean hayDisponibilidad(Reserva reserva);
    ReservaExtraccion extraerReserva(String frase);
}
