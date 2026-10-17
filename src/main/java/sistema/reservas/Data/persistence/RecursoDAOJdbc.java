package sistema.reservas.Data.persistence;

import sistema.reservas.Logic.Recurso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecursoDAOJdbc implements RecursoDAO {

    private final Connection conexion;   // la abre y la cierra el Worker (D11); nunca estática

    public RecursoDAOJdbc(Connection conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula.");
        }
        this.conexion = conexion;
    }

    @Override
    public void insertar(Recurso recurso) {
        String sql = "INSERT INTO recurso (id, nombre, descripcion, id_categoria) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, recurso.getId());
            ps.setString(2, recurso.getNombre());
            ps.setString(3, recurso.getDescripcion());
            ps.setInt(4, recurso.getCategoria().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "guardar el recurso", "Ya existe un recurso con el ID indicado.", null);
        }
    }

    @Override
    public Recurso buscarPorId(String id) {
        List<Recurso> resultado = consultar(" WHERE r.id = ?", id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public List<Recurso> listar() {
        return consultar("");
    }

    @Override
    public List<Recurso> listarPorCategoria(int idCategoria) {
        return consultar(" WHERE r.id_categoria = ?", idCategoria);
    }

    @Override
    public void actualizar(Recurso recurso) {
        String sql = "UPDATE recurso SET nombre = ?, descripcion = ?, id_categoria = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, recurso.getNombre());
            ps.setString(2, recurso.getDescripcion());
            ps.setInt(3, recurso.getCategoria().getId());
            ps.setString(4, recurso.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("El recurso no existe.");
            }
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "modificar el recurso", null, null);
        }
    }

    @Override
    public void eliminar(String id) {
        try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM recurso WHERE id = ?")) {
            ps.setString(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("El recurso no existe.");
            }
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "eliminar el recurso", null,
                    "No se puede eliminar el recurso: tiene reservas asociadas.");
        }
    }

    /** 'filtro' es siempre texto fijo del DAO*/
    private List<Recurso> consultar(String filtro, Object... parametros) {
        String sql = Mapeos.SELECT_RECURSO + filtro + " ORDER BY r.id";
        List<Recurso> recursos = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recursos.add(Mapeos.recurso(rs));
                }
            }
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "consultar los recursos", null, null);
        }
        return recursos;
    }
}