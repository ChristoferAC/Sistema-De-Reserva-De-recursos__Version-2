package sistema.reservas.unit;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sistema.reservas.Data.persistence.XmlUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Soporte para las pruebas de RecursoService/ReservaService, que ahora
 * persisten directamente en archivos XML reales (ya no reciben un DAO
 * inyectable). Respalda los archivos de datos reales antes de correr y
 * los restaura después, para no perder información del proyecto ni
 * dejar basura de las pruebas.
 */
public final class XmlTestDataSupport {

    public static final String RUTA_USUARIOS = "data/usuarios.xml";
    public static final String RUTA_CATEGORIAS = "data/categorias.xml";
    public static final String RUTA_RECURSOS = "data/recursos.xml";
    public static final String RUTA_RESERVAS = "data/reservas.xml";

    private static final String[] RUTAS = {RUTA_USUARIOS, RUTA_CATEGORIAS, RUTA_RECURSOS, RUTA_RESERVAS};

    private XmlTestDataSupport() {
    }

    /** Respalda (renombra a .bak) los archivos de datos reales, si existen. */
    public static void respaldar() {
        for (String ruta : RUTAS) {
            moverSiExiste(ruta, ruta + ".bak");
        }
    }

    /** Borra los archivos de prueba y restaura el respaldo original. */
    public static void restaurar() {
        for (String ruta : RUTAS) {
            new File(ruta).delete();
            moverSiExiste(ruta + ".bak", ruta);
        }
    }

    /** Borra los archivos de datos para que cada prueba arranque desde cero. */
    public static void limpiar() {
        for (String ruta : RUTAS) {
            new File(ruta).delete();
        }
    }

    public static void seedCategoria(int id, String nombre, String descripcion) {
        Document doc = XmlUtil.cargarOCrear(RUTA_CATEGORIAS, "categorias");
        Element raiz = doc.getDocumentElement();

        Element item = doc.createElement("categoria");
        XmlUtil.agregarTexto(doc, item, "id", String.valueOf(id));
        XmlUtil.agregarTexto(doc, item, "nombre", nombre);
        XmlUtil.agregarTexto(doc, item, "descripcion", descripcion);
        raiz.appendChild(item);

        XmlUtil.guardar(doc, RUTA_CATEGORIAS);
    }

    public static void seedFuncionario(int id, String nombre, String username, String password, String telefono) {
        Document doc = XmlUtil.cargarOCrear(RUTA_USUARIOS, "usuarios");
        Element raiz = doc.getDocumentElement();

        Element item = doc.createElement("usuario");
        item.setAttribute("tipo", "FUNCIONARIO");
        XmlUtil.agregarTexto(doc, item, "id", String.valueOf(id));
        XmlUtil.agregarTexto(doc, item, "nombre", nombre);
        XmlUtil.agregarTexto(doc, item, "username", username);
        XmlUtil.agregarTexto(doc, item, "password", password);
        XmlUtil.agregarTexto(doc, item, "telefono", telefono);
        raiz.appendChild(item);

        XmlUtil.guardar(doc, RUTA_USUARIOS);
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