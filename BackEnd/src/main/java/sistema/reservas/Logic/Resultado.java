package sistema.reservas.Logic;

/**
 * Lo que devuelve un Handler al Worker.
 * Algunas operaciones del Frontend esperan un objeto de vuelta (ej. findAll) y otras no
 * (ej. delete): el Proxy lee la respuesta segun el caso, por eso el Worker necesita saber
 * si hay valor que escribir.
 */
public record Resultado(boolean conValor, Object valor) {

    public static final Resultado VACIO = new Resultado(false, null);

    public static Resultado de(Object valor) {
        return new Resultado(true, valor);
    }
}