package sistema.reservas.Data.persistence;

import java.sql.SQLException;

final class SqlErrores {

    private SqlErrores() {}

    static RuntimeException traducir(SQLException e, String operacion, String msgDuplicado, String msgEnUso) {
        switch (e.getErrorCode()) {
            case 1062: // clave duplicada
                return new IllegalArgumentException(
                        msgDuplicado != null ? msgDuplicado : "Ya existe un registro con esos datos.");
            case 1451: // Referencia otras filas
                return new IllegalArgumentException(
                        msgEnUso != null ? msgEnUso : "No se puede completar la operación: el registro está en uso.");
            case 1452: // Referencia a una fila que no existe
                return new IllegalArgumentException(
                        "La categoría, el funcionario o el recurso indicado no existe.");
            default:
                return new IllegalStateException(
                        "No se pudo " + operacion + " en la base de datos: " + e.getMessage(), e);
        }
    }
}
