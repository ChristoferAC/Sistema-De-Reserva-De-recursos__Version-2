package test.java.sistema.reservas.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.Data.persistence.ReservaDAOJdbc;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba la "estructura fija" de la transacción de ReservaDAOJdbc (autocommit(false) /
 * commit / rollback en el catch / autocommit(true) en el finally) con una Connection
 * simulada que falla a propósito. No toca MySQL: la prueba contra la base real la hace
 * el IT (reservas_test).
 */
class ReservaDAOJdbcTransaccionTest {

    private ConexionSimulada cx;
    private Reserva reserva;

    @BeforeEach
    void setUp() {
        cx = new ConexionSimulada();
        CategoriaRecurso categoria = new CategoriaRecurso(1, "", "Laptops");
        reserva = new Reserva(0, new Funcionario(1234, "Juan", "1234", null, "8888"), "Reunión",
                LocalDate.of(2030, 1, 15), LocalTime.of(9, 0), LocalTime.of(10, 0));
        reserva.agregarCategoria(categoria);
        reserva.agregarRecurso(new Recurso("1111", "Laptop", "Laptop Dell", categoria));
    }

    // ------------------------------- insertar -------------------------------

    @Test
    void insertar_exitoso_hacePasosEnOrdenYHaceCommit() {
        new ReservaDAOJdbc(cx.conexion()).insertar(reserva);

        assertEquals(42, reserva.getId());   // el id lo asignó la "BD"
        List<String> e = cx.eventos;
        assertTrue(e.indexOf("autocommit=false") < e.indexOf("commit"));
        assertTrue(e.indexOf("commit") < e.lastIndexOf("autocommit=true"));
        assertFalse(e.contains("rollback"));
        assertEquals("autocommit=true", e.get(e.size() - 1));   // lo último: restaurar
        assertEquals(3, cx.cuantasSentenciasContienen("INSERT INTO"));   // cabecera + categoría + recurso
    }

    @Test
    void insertar_fallaAMedioCamino_haceRollback_noCommit_yRestauraAutocommit() {
        cx.fallaCuandoSqlContiene = "reserva_recurso";
        cx.codigoDeError = 1452;   // llave foránea: el recurso no existe

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new ReservaDAOJdbc(cx.conexion()).insertar(reserva));

        assertEquals("La categoría, el funcionario o el recurso indicado no existe.", ex.getMessage());
        assertTrue(cx.eventos.contains("rollback"));
        assertFalse(cx.eventos.contains("commit"));
        assertEquals("autocommit=true", cx.eventos.get(cx.eventos.size() - 1));
        assertEquals(0, reserva.getId());   // el id temporal no se queda en el objeto
    }

    @Test
    void insertar_errorGenericoDeBD_seTraduceYRevierte() {
        cx.fallaCuandoSqlContiene = "reserva_categoria";
        cx.codigoDeError = 1205;   // p. ej. timeout de bloqueo: sin traducción especial

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> new ReservaDAOJdbc(cx.conexion()).insertar(reserva));

        assertTrue(ex.getMessage().startsWith("No se pudo guardar la reserva en la base de datos"));
        assertTrue(cx.eventos.contains("rollback"));
        assertEquals("autocommit=true", cx.eventos.get(cx.eventos.size() - 1));
    }

    @Test
    void insertar_siElRollbackTambienFalla_igualRestauraAutocommit_yLanzaElErrorOriginal() {
        cx.fallaCuandoSqlContiene = "reserva_recurso";
        cx.codigoDeError = 1452;
        cx.fallaElRollback = true;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new ReservaDAOJdbc(cx.conexion()).insertar(reserva));

        assertEquals("La categoría, el funcionario o el recurso indicado no existe.", ex.getMessage());
        assertEquals("autocommit=true", cx.eventos.get(cx.eventos.size() - 1));
    }

    // ------------------------------- actualizar -------------------------------

    @Test
    void actualizar_exitoso_reemplazaElDetalleEnLaMismaTransaccion() {
        reserva.setId(7);

        new ReservaDAOJdbc(cx.conexion()).actualizar(reserva);

        List<String> e = cx.eventos;
        assertEquals(1, cx.cuantasSentenciasContienen("UPDATE reserva"));
        assertEquals(2, cx.cuantasSentenciasContienen("DELETE FROM"));   // categorías y recursos viejos
        assertEquals(2, cx.cuantasSentenciasContienen("INSERT INTO"));   // categorías y recursos nuevos
        assertTrue(e.indexOf("autocommit=false") < e.indexOf("commit"));
        assertFalse(e.contains("rollback"));
        assertEquals("autocommit=true", e.get(e.size() - 1));
    }

    @Test
    void actualizar_reservaInexistente_revierteYAvisa() {
        reserva.setId(7);
        cx.filasDelUpdate = 0;   // el UPDATE no encontró la fila

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new ReservaDAOJdbc(cx.conexion()).actualizar(reserva));

        assertEquals("La reserva no existe.", ex.getMessage());
        assertTrue(cx.eventos.contains("rollback"));
        assertFalse(cx.eventos.contains("commit"));
        assertEquals("autocommit=true", cx.eventos.get(cx.eventos.size() - 1));
        assertEquals(0, cx.cuantasSentenciasContienen("DELETE FROM"));   // no tocó el detalle
    }

    @Test
    void actualizar_fallaAlReinsertarElDetalle_noDejaElDetalleAMedias() {
        reserva.setId(7);
        cx.fallaCuandoSqlContiene = "INSERT INTO reserva_recurso";
        cx.codigoDeError = 1452;

        assertThrows(IllegalArgumentException.class,
                () -> new ReservaDAOJdbc(cx.conexion()).actualizar(reserva));

        assertTrue(cx.eventos.contains("rollback"));   // los DELETE previos también se deshacen
        assertFalse(cx.eventos.contains("commit"));
        assertEquals("autocommit=true", cx.eventos.get(cx.eventos.size() - 1));
    }

    // ----------------------- Connection simulada (java.lang.reflect.Proxy) -----------------------

    static class ConexionSimulada {
        final List<String> eventos = new ArrayList<>();
        String fallaCuandoSqlContiene = null;
        int codigoDeError = 0;
        boolean fallaElRollback = false;
        int filasDelUpdate = 1;

        int cuantasSentenciasContienen(String texto) {
            int n = 0;
            for (String evento : eventos) {
                if (evento.startsWith("SQL:") && evento.contains(texto)) {
                    n++;
                }
            }
            return n;
        }

        Connection conexion() {
            InvocationHandler manejador = (proxy, metodo, args) -> {
                switch (metodo.getName()) {
                    case "setAutoCommit":
                        eventos.add("autocommit=" + args[0]);
                        return null;
                    case "commit":
                        eventos.add("commit");
                        return null;
                    case "rollback":
                        eventos.add("rollback");
                        if (fallaElRollback) {
                            throw new SQLException("rollback falló", "08000", 2013);
                        }
                        return null;
                    case "prepareStatement":
                        return sentencia((String) args[0]);
                    case "close":
                        return null;
                    default:
                        throw new UnsupportedOperationException("Connection." + metodo.getName());
                }
            };
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class}, manejador);
        }

        private PreparedStatement sentencia(String sql) {
            InvocationHandler manejador = (proxy, metodo, args) -> {
                String nombre = metodo.getName();
                if (nombre.equals("executeUpdate")) {
                    eventos.add("SQL:" + sql);
                    if (fallaCuandoSqlContiene != null && sql.contains(fallaCuandoSqlContiene)) {
                        throw new SQLException("fallo simulado", "23000", codigoDeError);
                    }
                    return sql.startsWith("UPDATE") ? filasDelUpdate : 1;
                }
                if (nombre.equals("getGeneratedKeys")) {
                    return llaves();
                }
                if (nombre.equals("close") || nombre.startsWith("set")) {
                    return null;
                }
                throw new UnsupportedOperationException("PreparedStatement." + nombre);
            };
            return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                    new Class<?>[]{PreparedStatement.class}, manejador);
        }

        private ResultSet llaves() {
            boolean[] entregada = {false};
            InvocationHandler manejador = (proxy, metodo, args) -> {
                switch (metodo.getName()) {
                    case "next":
                        boolean hay = !entregada[0];
                        entregada[0] = true;
                        return hay;
                    case "getInt":
                        return 42;
                    case "close":
                        return null;
                    default:
                        throw new UnsupportedOperationException("ResultSet." + metodo.getName());
                }
            };
            return (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(),
                    new Class<?>[]{ResultSet.class}, manejador);
        }
    }
}