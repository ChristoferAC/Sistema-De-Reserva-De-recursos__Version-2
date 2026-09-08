package sistema.reservas.Data.llm;

public class LLMService {
    public ReservaDTO interpretarReserva(String texto) {

        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto de la reserva no puede estar vacío.");
        }
        /*
         * TODO:
         *
         * 1. Enviar texto al modelo LLM.
         * 2. Recibir los datos estructurados.
         * 3. Convertirlos a ReservaDTO.
         * 4. Retornar el DTO.
         */
        throw new UnsupportedOperationException("La integración con el LLM todavía no ha sido implementada.");
    }
}
