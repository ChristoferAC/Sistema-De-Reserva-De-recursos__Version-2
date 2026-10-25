package sistema.reservas.Logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Contrato entre el Worker y los modulos (un Handler por integrante).
 * Tiene la MISMA firma que ya usa UsuariosHandler.atender(...) de I1, asi que se puede
 * enchufar sin modificar su clase:   handlers.add(usuariosHandler::atender);
 *
 * Reglas:
 *  - Si el codigo NO es de este handler: devolver false SIN leer nada del stream.
 *  - Si es suyo: leer sus argumentos, ejecutar, escribir la respuesta (OK o ERROR),
 *    hacer flush y devolver true.
 */
@FunctionalInterface
public interface Handler {

    boolean atender(int codigo, ObjectInputStream is, ObjectOutputStream os) throws IOException;
}