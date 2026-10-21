package sistema.reservas.scratch;


import sistema.reservas.Data.persistence.RecursoDAOJdbc;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Services.ServiceRecurso;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;

public class PruebaRecursoMySQL {

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
            ServiceRecurso service = new ServiceRecurso(new RecursoDAOJdbc(cx), Collections::emptyList);
            CategoriaRecurso categoria1 = new CategoriaRecurso(1, "", "Elementos Electronicos");

            try {
                verificar("semilla cargada (7 recursos)", service.listarRecursos().size() >= 7);

                service.crearRecurso(new Recurso("TEMP-1", "Recurso temporal", "Prueba paso 3", categoria1));
                Recurso leido = service.buscarRecurso("TEMP-1");
                verificar("crear y buscar", leido != null && "Recurso temporal".equals(leido.getNombre()));

                esperarError("ID duplicado",
                        () -> service.crearRecurso(new Recurso("TEMP-1", "Otro", "Otro", categoria1)),
                        "Ya existe");

                esperarError("categoría inexistente (FK 1452)",
                        () -> service.crearRecurso(new Recurso("TEMP-2", "X", "X",
                                new CategoriaRecurso(999, "", "No existe"))),
                        "no existe");

                boolean todosDeCategoria1 = service.listarPorCategoria(1).stream()
                        .allMatch(r -> r.getCategoria().getId() == 1);
                verificar("listarPorCategoria devuelve solo la categoría 1",
                        todosDeCategoria1 && service.listarPorCategoria(1).stream()
                                .anyMatch(r -> "TEMP-1".equals(r.getId())));

                leido.setDescripcion("Descripción modificada");
                service.modificarRecurso(leido);
                verificar("modificar", "Descripción modificada".equals(service.buscarRecurso("TEMP-1").getDescripcion()));

                esperarError("eliminar un recurso con reservas (FK 1451)",
                        () -> service.eliminarRecurso("1111"), "reservas asociadas");

                service.eliminarRecurso("TEMP-1");
                verificar("eliminar", service.buscarRecurso("TEMP-1") == null);

                esperarError("eliminar inexistente", () -> service.eliminarRecurso("TEMP-1"), "no existe");

                System.out.println("\nPASO 3: todo OK.");
            } finally {
                try {
                    service.eliminarRecurso("TEMP-1");   // limpieza por si algo falló a medias
                } catch (RuntimeException ignorar) {
                    // ya no existe
                }
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