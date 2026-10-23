// SCRATCH: no se commitea (la carpeta scratch va en .git/info/exclude).
package sistema.reservas.scratch;

import sistema.reservas.Data.persistence.ReservaDAOJdbc;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Paso 4 de la guía, contra MySQL real (reservas_test): inserta, lee, modifica y cancela una
 * reserva, y FUERZA un fallo a medio camino (recurso inexistente) para comprobar que el
 * rollback no deja ninguna fila. Variables de entorno: DB_USER, DB_PASS (y DB_URL opcional).
 * Requiere la semilla de sql/02 (reserva 1111, recursos 1111 y 1112, funcionario 1234).
 */
public class PruebaReservaMySQL {

    private static final String MARCA = "TEMP-RESERVA";
    private static final LocalDate DIA = LocalDate.of(2030, 1, 15);

    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/reservas_test?serverTimezone=America/Costa_Rica");
        String base = url.substring(url.lastIndexOf('/') + 1);
        if (base.contains("?")) {
            base = base.substring(0, base.indexOf('?'));
        }
        if (!base.endsWith("_test")) {   // regla del equipo: pruebas solo contra *_test
            throw new IllegalStateException("Esta prueba solo corre contra una base *_test. Base: " + base);
        }

        try (Connection cx = DriverManager.getConnection(url, System.getenv("DB_USER"), System.getenv("DB_PASS"))) {
            ReservaDAOJdbc dao = new ReservaDAOJdbc(cx);
            limpiar(cx);   // por si quedó algo de una corrida anterior

            try {
                CategoriaRecurso categoria1 = new CategoriaRecurso(1, "", "Elementos Electronicos");
                Funcionario funcionario = new Funcionario(1234, "1234", "1234", null, "1234");

                // 1) Insertar una reserva válida: el id lo asigna la BD y se lee completa.
                Reserva r = nueva(funcionario, categoria1, "1111", MARCA + " ok", 14, 15);
                dao.insertar(r);
                verificar("insertar: la BD asigna un id nuevo (mayor que 1111, el de la semilla)", r.getId() > 1111);

                Reserva leida = dao.buscarPorId(r.getId());
                verificar("leer: cabecera, 1 categoría y el recurso 1111",
                        leida != null
                                && (MARCA + " ok").equals(leida.getActividad())
                                && DIA.equals(leida.getFecha())
                                && LocalTime.of(14, 0).equals(leida.getHoraInicio())
                                && leida.getCategoriasSolicitadas().size() == 1
                                && leida.getRecursosAsignados().size() == 1
                                && "1111".equals(leida.getRecursosAsignados().get(0).getId()));
                verificar("leer: el funcionario llega SIN clave (H3)", leida.getFuncionario().getPassword() == null);
                verificar("leer: estado ACTIVA", leida.isActiva());

                // 2) ROLLBACK forzado: recurso inexistente a medio camino.
                int reservasAntes = contar(cx, "SELECT COUNT(*) FROM reserva");
                int categoriasAntes = contar(cx, "SELECT COUNT(*) FROM reserva_categoria");
                int recursosAntes = contar(cx, "SELECT COUNT(*) FROM reserva_recurso");
                Reserva mala = nueva(funcionario, categoria1, "NO-EXISTE", MARCA + " mala", 16, 17);
                esperarError("recurso inexistente a medio camino (FK 1452)", () -> dao.insertar(mala), "no existe");
                verificar("rollback: no quedó ninguna fila de esa reserva",
                        contar(cx, "SELECT COUNT(*) FROM reserva WHERE actividad = ?", MARCA + " mala") == 0);
                verificar("rollback: los conteos de reserva, categorías y recursos no cambiaron",
                        contar(cx, "SELECT COUNT(*) FROM reserva") == reservasAntes
                                && contar(cx, "SELECT COUNT(*) FROM reserva_categoria") == categoriasAntes
                                && contar(cx, "SELECT COUNT(*) FROM reserva_recurso") == recursosAntes);
                verificar("el autocommit se restauró a true", cx.getAutoCommit());
                verificar("el id temporal no quedó en el objeto", mala.getId() == 0);

                // 3) Modificar: cambia la actividad y reemplaza el recurso (1111 -> 1112).
                leida.setActividad(MARCA + " modificada");
                leida.limpiarRecursosAsignados();
                leida.agregarRecurso(new Recurso("1112", "Proyector", "Proyector", categoria1));
                dao.actualizar(leida);
                Reserva modificada = dao.buscarPorId(r.getId());
                verificar("modificar: nueva actividad y recurso 1112, la categoría se conserva",
                        (MARCA + " modificada").equals(modificada.getActividad())
                                && modificada.getRecursosAsignados().size() == 1
                                && "1112".equals(modificada.getRecursosAsignados().get(0).getId())
                                && modificada.getCategoriasSolicitadas().size() == 1);

                // 4) Cancelar = estado CANCELADA y sin recursos; la fila NO se borra.
                modificada.setEstado(Reserva.Estado.CANCELADA);
                modificada.limpiarRecursosAsignados();
                dao.actualizar(modificada);
                Reserva cancelada = dao.buscarPorId(r.getId());
                verificar("cancelar: sigue existiendo, CANCELADA, sin recursos, con su categoría",
                        cancelada != null
                                && Reserva.Estado.CANCELADA == cancelada.getEstado()
                                && cancelada.getRecursosAsignados().isEmpty()
                                && cancelada.getCategoriasSolicitadas().size() == 1);

                // 5) Actualizar una reserva inexistente: avisa y revierte.
                Reserva fantasma = nueva(funcionario, categoria1, "1111", MARCA + " fantasma", 9, 10);
                fantasma.setId(999999);
                esperarError("actualizar una reserva inexistente", () -> dao.actualizar(fantasma), "no existe");
                verificar("el autocommit sigue en true", cx.getAutoCommit());

                // 6) Listados.
                verificar("listarPorFecha trae la reserva semilla 1111 (2026-09-10)",
                        dao.listarPorFecha(LocalDate.of(2026, 9, 10)).stream().anyMatch(x -> x.getId() == 1111));
                verificar("listarPorFecha trae también la cancelada (el Service filtra las ACTIVA)",
                        dao.listarPorFecha(DIA).stream().anyMatch(x -> x.getId() == r.getId()));
                verificar("listarPorFuncionario(1234) trae la semilla y la de prueba",
                        dao.listarPorFuncionario(1234).stream().anyMatch(x -> x.getId() == 1111)
                                && dao.listarPorFuncionario(1234).stream().anyMatch(x -> x.getId() == r.getId()));

                System.out.println("\nPASO 4: todo OK.");
            } finally {
                limpiar(cx);   // deja la base como estaba (solo las filas TEMP-RESERVA)
            }
        }
    }

    private static Reserva nueva(Funcionario f, CategoriaRecurso categoria, String idRecurso,
                                 String actividad, int horaInicio, int horaFin) {
        Reserva r = new Reserva(0, f, actividad, DIA, LocalTime.of(horaInicio, 0), LocalTime.of(horaFin, 0));
        r.agregarCategoria(categoria);
        r.agregarRecurso(new Recurso(idRecurso, "x", "x", categoria));
        return r;
    }

    private static void limpiar(Connection cx) throws Exception {
        String like = MARCA + "%";
        borrar(cx, "DELETE FROM reserva_recurso WHERE id_reserva IN (SELECT id FROM reserva WHERE actividad LIKE ?)", like);
        borrar(cx, "DELETE FROM reserva_categoria WHERE id_reserva IN (SELECT id FROM reserva WHERE actividad LIKE ?)", like);
        borrar(cx, "DELETE FROM reserva WHERE actividad LIKE ?", like);
    }

    private static void borrar(Connection cx, String sql, String parametro) throws Exception {
        try (PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, parametro);
            ps.executeUpdate();
        }
    }

    private static int contar(Connection cx, String sql, Object... parametros) throws Exception {
        try (PreparedStatement ps = cx.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (!condicion) {
            throw new AssertionError("FALLÓ: " + descripcion);
        }
        System.out.println("OK  " + descripcion);
    }

    private static void esperarError(String descripcion, Runnable accion, String fragmentoDelMensaje) {
        try {
            accion.run();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains(fragmentoDelMensaje)) {
                System.out.println("OK  " + descripcion + " -> " + e.getMessage());
                return;
            }
            throw new AssertionError(descripcion + ": mensaje inesperado: " + e.getMessage(), e);
        }
        throw new AssertionError(descripcion + ": debía fallar y no falló");
    }
}