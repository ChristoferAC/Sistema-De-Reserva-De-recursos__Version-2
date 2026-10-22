package sistema.reservas.Data.persistence;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservaDAOJdbc implements ReservaDAO {

    private static final Logger LOG = Logger.getLogger(ReservaDAOJdbc.class.getName());

    private static final String SELECT_RESERVA =
            "SELECT r.id, r.actividad, r.fecha, r.hora_inicio, r.hora_fin, r.estado, "
                    + "u.id AS f_id, u.nombre AS f_nombre, u.username AS f_username, u.telefono AS f_telefono "
                    + "FROM reserva r JOIN usuario u ON u.id = r.id_funcionario";

    private final Connection conexion;   // la abre y la cierra el Worker (D11); nunca estática

    public ReservaDAOJdbc(Connection conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula.");
        }
        this.conexion = conexion;
    }

    // ------------------------- Write-------------------------
    @Override
    public Reserva insertar(Reserva reserva) {
        int idOriginal = reserva.getId();
        try {
            conexion.setAutoCommit(false);
            reserva.setId(insertarCabecera(reserva)); // la asigna la BD
            insertarDetalle(reserva);
            conexion.commit();
            return reserva;
        } catch (SQLException e) {
            reserva.setId(idOriginal);
            revertir();
            throw SqlErrores.traducir(e, "guardar la reserva", null, null);
        } catch (RuntimeException e) {
            reserva.setId(idOriginal);
            revertir();
            throw e;
        } finally {
            restaurarAutoCommit();
        }
    }

    @Override
    public void actualizar(Reserva reserva) {
        String sql = "UPDATE reserva SET id_funcionario = ?, actividad = ?, fecha = ?, "
                + "hora_inicio = ?, hora_fin = ?, estado = ? WHERE id = ?";
        try {
            conexion.setAutoCommit(false);
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                asignarCabecera(ps, reserva);
                ps.setInt(7, reserva.getId());
                if (ps.executeUpdate() == 0) {
                    throw new IllegalArgumentException("La reserva no existe.");
                }
            }
            borrarDetalle(reserva.getId());
            insertarDetalle(reserva);
            conexion.commit();
        } catch (SQLException e) {
            revertir();
            throw SqlErrores.traducir(e, "modificar la reserva", null, null);
        } catch (RuntimeException e) {
            revertir();
            throw e;
        } finally {
            restaurarAutoCommit();
        }
    }

    private int insertarCabecera(Reserva reserva) throws SQLException {
        String sql = "INSERT INTO reserva (id_funcionario, actividad, fecha, hora_inicio, hora_fin, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarCabecera(ps, reserva);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
        }
        throw new SQLException("La base de datos no devolvió el id generado de la reserva.");
    }

    private void asignarCabecera(PreparedStatement ps, Reserva r) throws SQLException {
        ps.setInt(1, r.getFuncionario().getId());
        ps.setString(2, r.getActividad());
        ps.setObject(3, r.getFecha());
        ps.setObject(4, r.getHoraInicio());
        ps.setObject(5, r.getHoraFin());
        ps.setString(6, r.getEstado().name());
    }

    private void insertarDetalle(Reserva r) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "INSERT INTO reserva_categoria (id_reserva, id_categoria) VALUES (?, ?)")) {
            for (CategoriaRecurso categoria : r.getCategoriasSolicitadas()) {
                ps.setInt(1, r.getId());
                ps.setInt(2, categoria.getId());
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES (?, ?)")) {
            for (Recurso recurso : r.getRecursosAsignados()) {
                ps.setInt(1, r.getId());
                ps.setString(2, recurso.getId());
                ps.executeUpdate();
            }
        }
    }

    private void borrarDetalle(int idReserva) throws SQLException {
        try (PreparedStatement ps = conexion.prepareStatement(
                "DELETE FROM reserva_categoria WHERE id_reserva = ?")) {
            ps.setInt(1, idReserva);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "DELETE FROM reserva_recurso WHERE id_reserva = ?")) {
            ps.setInt(1, idReserva);
            ps.executeUpdate();
        }
    }

    private void revertir() {
        try {
            conexion.rollback();
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "No se pudo revertir la transacción de reserva", ex);
        }
    }

    private void restaurarAutoCommit() {
        try {
            conexion.setAutoCommit(true);
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, "No se pudo restaurar el autocommit", ex);
        }
    }

    // ------------------------------- Read -------------------------------

    @Override
    public Reserva buscarPorId(int id) {
        List<Reserva> resultado = consultar(" WHERE r.id = ?", id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public List<Reserva> listar() {
        return consultar("");
    }

    @Override
    public List<Reserva> listarPorFuncionario(int idFuncionario) {
        return consultar(" WHERE r.id_funcionario = ?", idFuncionario);
    }

    @Override
    public List<Reserva> listarPorFecha(LocalDate fecha) {
        return consultar(" WHERE r.fecha = ?", fecha);
    }

    /** 'filtro' es siempre texto fijo del DAO; los datos del usuario van como parámetros. */
    private List<Reserva> consultar(String filtro, Object... parametros) {
        String sql = SELECT_RESERVA + filtro + " ORDER BY r.id";
        List<Reserva> reservas = new ArrayList<>();
        try {
            try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                for (int i = 0; i < parametros.length; i++) {
                    ps.setObject(i + 1, parametros[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        reservas.add(leerCabecera(rs));
                    }
                }
            }
            for (Reserva reserva : reservas) {   // Cdon el ResultSet anterior ya cerrado
                cargarCategorias(reserva);
                cargarRecursos(reserva);
            }
            return reservas;
        } catch (SQLException e) {
            throw SqlErrores.traducir(e, "consultar las reservas", null, null);
        }
    }

    private Reserva leerCabecera(ResultSet rs) throws SQLException {
        Funcionario funcionario = new Funcionario(
                rs.getInt("f_id"), rs.getString("f_nombre"),
                rs.getString("f_username"), null, rs.getString("f_telefono"));   // password null (H3)

        Reserva reserva = new Reserva(
                rs.getInt("id"), funcionario, rs.getString("actividad"),
                rs.getObject("fecha", LocalDate.class),
                rs.getObject("hora_inicio", LocalTime.class),
                rs.getObject("hora_fin", LocalTime.class));
        reserva.setEstado(Reserva.Estado.valueOf(rs.getString("estado")));
        return reserva;
    }

    private void cargarCategorias(Reserva reserva) throws SQLException {
        String sql = "SELECT c.id, c.nombre, c.descripcion FROM reserva_categoria rc "
                + "JOIN categoria c ON c.id = rc.id_categoria WHERE rc.id_reserva = ? ORDER BY c.id";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, reserva.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reserva.agregarCategoria(Mapeos.categoria(
                            rs.getInt("id"), rs.getString("nombre"), rs.getString("descripcion")));
                }
            }
        }
    }

    private void cargarRecursos(Reserva reserva) throws SQLException {
        String sql = Mapeos.SELECT_RECURSO
                + " JOIN reserva_recurso rr ON rr.id_recurso = r.id WHERE rr.id_reserva = ? ORDER BY r.id";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, reserva.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reserva.agregarRecurso(Mapeos.recurso(rs));
                }
            }
        }
    }
}