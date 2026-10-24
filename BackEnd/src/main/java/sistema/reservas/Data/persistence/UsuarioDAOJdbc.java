// ============================================================
// BACKEND / src/main/java/sistema/reservas/Data/persistence/UsuarioDAOJdbc.java
// ============================================================
package sistema.reservas.Data.persistence;

import sistema.reservas.Logic.Administrador;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAOJdbc implements UsuarioDAO {

    // Nunca se selecciona la columna password (regla 5)
    private static final String SELECT = "SELECT id, nombre, username, rol, telefono FROM usuario";

    private final Connection conexion;   // la abre y la cierra el Worker (D11); nunca estática

    public UsuarioDAOJdbc(Connection conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula.");
        }
        this.conexion = conexion;
    }

    @Override
    public Usuario buscarPorCredenciales(String username, String clave) {
        // La clave se compara en MySQL: nunca viaja de vuelta en un objeto
        List<Usuario> resultado = consultar(" WHERE username = ? AND password = ?", username, clave);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public Usuario buscarPorId(int id) {
        List<Usuario> resultado = consultar(" WHERE id = ?", id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public List<Usuario> listar() {
        return consultar("");
    }

    @Override
    public void insertar(Usuario usuario, String clave) {
        String sql = "INSERT INTO usuario (id, nombre, username, password, rol, telefono) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, usuario.getId());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getUsername());
            ps.setString(4, clave);
            ps.setString(5, usuario.getRol());
            ps.setString(6, telefonoDe(usuario));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "guardar el usuario",
                    "Ya existe un usuario con ese ID o con ese nombre de usuario.", null);
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre = ?, username = ?, telefono = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getUsername());
            ps.setString(3, telefonoDe(usuario));
            ps.setInt(4, usuario.getId());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe un usuario con ese ID.");
            }
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "modificar el usuario",
                    "Ya existe otro usuario con ese nombre de usuario.", null);
        }
    }

    @Override
    public void actualizarClave(int id, String claveNueva) {
        try (PreparedStatement ps = conexion.prepareStatement("UPDATE usuario SET password = ? WHERE id = ?")) {
            ps.setString(1, claveNueva);
            ps.setInt(2, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe un usuario con ese ID.");
            }
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "cambiar la clave", null, null);
        }
    }

    @Override
    public void eliminar(int id) {
        try (PreparedStatement ps = conexion.prepareStatement("DELETE FROM usuario WHERE id = ?")) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe un usuario con ese ID.");
            }
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "eliminar el usuario", null,
                    "No se puede borrar: tiene reservas.");
        }
    }

    /** 'filtro' es siempre texto fijo del DAO; los datos del usuario van como parámetros. */
    private List<Usuario> consultar(String filtro, Object... parametros) {
        String sql = SELECT + filtro + " ORDER BY id";
        List<Usuario> usuarios = new ArrayList<>();
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(from(rs));
                }
            }
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "consultar los usuarios", null, null);
        }
        return usuarios;
    }

    /** Arma un Administrador o un Funcionario según la columna rol; password siempre null. */
    private Usuario from(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String username = rs.getString("username");
        String rol = rs.getString("rol");

        if ("ADMIN".equals(rol)) {
            return new Administrador(id, nombre, username, null);
        }
        if ("FUNCIONARIO".equals(rol)) {
            return new Funcionario(id, nombre, username, null, rs.getString("telefono"));
        }
        throw new IllegalStateException("El usuario " + id + " tiene un rol desconocido: " + rol);
    }

    private String telefonoDe(Usuario usuario) {
        return usuario instanceof Funcionario ? ((Funcionario) usuario).getTelefono() : null;
    }
}