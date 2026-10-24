// ============================================================
// BACKEND / src/main/java/sistema/reservas/Data/persistence/CategoriaDAOJdbc.java
// Usa Mapeos.categoria y SqlErrores (mismo paquete), como RecursoDAOJdbc.
// ============================================================
package sistema.reservas.Data.persistence;

import sistema.reservas.Logic.CategoriaRecurso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAOJdbc implements CategoriaDAO {

    private static final String SELECT = "SELECT id, nombre, descripcion FROM categoria";

    private final Connection conexion;   // la abre y la cierra el Worker (D11); nunca estática

    public CategoriaDAOJdbc(Connection conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula.");
        }
        this.conexion = conexion;
    }

    @Override
    public CategoriaRecurso insertar(CategoriaRecurso categoria) {
        String sql = "INSERT INTO categoria (nombre, descripcion) VALUES (?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    categoria.setId(claves.getInt(1));
                    return categoria;
                }
            }
            throw new IllegalStateException("La base de datos no devolvió el id generado de la categoría.");
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "guardar la categoría", null, null);
        }
    }

    @Override
    public CategoriaRecurso buscarPorId(int id) {
        List<CategoriaRecurso> resultado = consultar(" WHERE id = ?", id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public List<CategoriaRecurso> buscarPorDescripcion(String texto) {
        return consultar(" WHERE descripcion LIKE ?", "%" + texto + "%");
    }

    @Override
    public List<CategoriaRecurso> listar() {
        return consultar("");
    }

    @Override
    public void actualizar(CategoriaRecurso categoria) {
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setInt(3, categoria.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe una categoría con ese ID.");
            }
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "modificar la categoría", null, null);
        }
    }

    @Override
    public void eliminar(int id) {
        try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM categoria WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe una categoría con ese ID.");
            }
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "eliminar la categoría", null,
                    "No se puede eliminar la categoría: tiene recursos o reservas asociadas.");
        }
    }

    /** 'filtro' es siempre texto fijo del DAO; los datos del usuario van como parámetros. */
    private List<CategoriaRecurso> consultar(String filtro, Object... parametros) {
        String sql = SELECT + filtro + " ORDER BY id";
        List<CategoriaRecurso> categorias = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categorias.add(Mapeos.categoria(
                            rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion")));
                }
            }
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "consultar las categorías", null, null);
        }
        return categorias;
    }
}