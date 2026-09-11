package sistema.reservas.Presentation.Reserva;

import sistema.reservas.Data.llm.ReservaExtraccion;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Logic.Recurso;

import java.util.List;

public class ReservaController {
    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    public void crear(Reserva reserva) {
        reservaService.crearReserva(reserva);
    }

    public Reserva buscar(int id) {
        return reservaService.buscarReserva(id);
    }

    public List<Reserva> listar() {
        return reservaService.listarReservas();
    }

    public List<Reserva> listarPorFuncionario(int idFuncionario) {
        return reservaService.listarReservasFuncionario(idFuncionario);
    }

    public void modificar(Reserva reserva) {
        reservaService.modificarReserva(reserva);
    }

    public void cancelar(int id) {
        reservaService.cancelarReserva(id);
    }

    public List<Recurso> listarRecursos() {return reservaService.listarRecursos();}

    /** Delega en el Service, que invoca el LLM vía LangChain4j. */
    public ReservaExtraccion extraerConIA(String frase) {
        return reservaService.extraerReserva(frase);
    }
}