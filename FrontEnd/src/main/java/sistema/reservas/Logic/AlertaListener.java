package sistema.reservas.Logic;

/** Quien quiera enterarse de las reservas nuevas de los demas usuarios (lo implementa ControllerAlerta). */
public interface AlertaListener {

    /** Se invoca siempre en el hilo de Swing (EDT). */
    void alerta(Reserva reserva);
}