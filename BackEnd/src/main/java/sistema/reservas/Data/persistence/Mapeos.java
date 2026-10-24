package sistema.reservas.Data.persistence;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;

import java.sql.ResultSet;
import java.sql.SQLException;

final class Mapeos {

    static final String SELECT_RECURSO =
            "SELECT r.id, r.nombre, r.descripcion, "
                    + "c.id AS cat_id, c.nombre AS cat_nombre, c.descripcion AS cat_descripcion "
                    + "FROM recurso r JOIN categoria c ON c.id = r.id_categoria";

    private Mapeos() {
    }

    /** Las categorías se identifican por descripción (nombre suele venir vacío, como en categorias.xml). */
    static CategoriaRecurso categoria(int id, String nombre, String descripcion) {
        String n = (nombre == null || nombre.trim().isEmpty()) ? descripcion : nombre;
        return new CategoriaRecurso(id, n, descripcion);
    }

    static Recurso recurso(ResultSet rs) throws SQLException {
        CategoriaRecurso categoria = categoria(
                rs.getInt("cat_id"), rs.getString("cat_nombre"), rs.getString("cat_descripcion"));
        return new Recurso(rs.getString("id"), rs.getString("nombre"), rs.getString("descripcion"), categoria);
    }
}
