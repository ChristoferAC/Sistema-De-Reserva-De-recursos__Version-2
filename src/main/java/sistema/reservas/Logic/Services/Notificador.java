package sistema.reservas.Logic.Services;

import sistema.reservas.Logic.Reserva;

/**
 * PLACEHOLDER de I2. La interfaz real de I3 (backend/server, Fase 0)
 */
public interface Notificador {

    /** Avisa a todos los clientes activos menos al que originó la reserva. */
    void nuevaReserva(Reserva reserva, Object origen);
}