package sistema.reservas.unit;

import sistema.reservas.Data.persistence.CategoriaXmlPersister;
import sistema.reservas.Data.persistence.CategoriasData;
import sistema.reservas.Data.persistence.UsuarioXmlPersister;
import sistema.reservas.Data.persistence.UsuariosData;
import sistema.reservas.Data.persistence.XmlUtil;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Usuario;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class XmlTestDataSupport {

    public static final String RUTA_USUARIOS = "data/usuarios.xml";
    public static final String RUTA_CATEGORIAS = "data/categorias.xml";
    public static final String RUTA_RECURSOS = "data/recursos.xml";
    public static final String RUTA_RESERVAS = "data/reservas.xml";

    private static final String[] RUTAS = {RUTA_USUARIOS, RUTA_CATEGORIAS, RUTA_RECURSOS, RUTA_RESERVAS};

    private static final Object LOCK = new Object();
    private static int referencias = 0;
    private static boolean shutdownHookRegistrado = false;

    private XmlTestDataSupport() {}

    /** Respalda (renombra a .bak) los archivos de datos reales, si existen. */
    public static void respaldar() {
        synchronized (LOCK) {
            if (referencias == 0) {
                for (String ruta : RUTAS) {
                    moverSiExiste(ruta, ruta + ".bak");
                }
                registrarShutdownHookSiHaceFalta();
            }
            referencias++;
        }
    }

    /** Borra los archivos de prueba y restaura el respaldo original. */
    public static void restaurar() {
        synchronized (LOCK) {
            if (referencias > 0) {
                referencias--;
            }
            if (referencias == 0) {
                restaurarAhora();
            }
        }
    }

    /** Borra los archivos de datos para que cada prueba arranque desde cero. */
    public static void limpiar() {
        for (String ruta : RUTAS) {
            new File(ruta).delete();
        }
    }

    /**
     * Agrega una categoría de prueba usando el mismo persistidor JAXB
     * (CategoriaXmlPersister) que usa RecursoService/ReservaService
     * para leerla de vuelta.
     */
    public static void seedCategoria(int id, String nombre, String descripcion) {
        try {
            CategoriaXmlPersister persister = new CategoriaXmlPersister();
            CategoriasData data = persister.load();

            List<CategoriaRecurso> categorias = data.getCategorias();
            categorias.add(new CategoriaRecurso(id, nombre, descripcion));
            data.setCategorias(categorias);

            persister.store(data);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo sembrar la categoría de prueba.", e);
        }
    }

    /**
     * Agrega un funcionario de prueba usando el mismo persistidor JAXB
     * (UsuarioXmlPersister) que usa ReservaService para leerlo de
     * vuelta (incluye el xsi:type que JAXB necesita para distinguir
     * Funcionario de Administrador).
     */
    public static void seedFuncionario(int id, String nombre, String username, String password, String telefono) {
        try {
            UsuarioXmlPersister persister = new UsuarioXmlPersister();
            UsuariosData data = persister.load();

            List<Usuario> usuarios = data.getUsuarios();
            usuarios.add(new Funcionario(id, nombre, username, password, telefono));
            data.setUsuarios(usuarios);

            persister.store(data);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo sembrar el funcionario de prueba.", e);
        }
    }

    private static void restaurarAhora() {
        for (String ruta : RUTAS) {
            new File(ruta).delete();
            moverSiExiste(ruta + ".bak", ruta);
        }
    }

    private static void registrarShutdownHookSiHaceFalta() {
        if (shutdownHookRegistrado) {
            return;
        }
        shutdownHookRegistrado = true;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (LOCK) {
                if (referencias > 0) {
                    restaurarAhora();
                }
            }
        }));
    }

    private static void moverSiExiste(String origen, String destino) {
        File archivoOrigen = new File(origen);
        if (archivoOrigen.exists()) {
            try {
                Files.move(archivoOrigen.toPath(), Path.of(destino), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("No se pudo mover " + origen + " a " + destino, e);
            }
        }
    }
}