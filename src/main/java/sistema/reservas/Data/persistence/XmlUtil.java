package sistema.reservas.Data.persistence;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad COMPARTIDA para leer y escribir listas de datos en archivos XML.
 * No conoce ninguna entidad del modelo — cada DAO real (UsuarioDAOXml,
 * CategoriaRecursoDAOXml, RecursoDAOXml, etc.) la usa para no repetir la
 * lógica de lectura/escritura de XML.
 *
 * Ver /docs/CONTRATO_XML.md para las reglas completas del contrato.
 */
public final class XmlUtil {

    private XmlUtil() {
    }

    /**
     * Carga el documento XML en rutaArchivo. Si el archivo no existe
     * todavía, crea un documento nuevo con un elemento raíz vacío llamado
     * elementoRaiz (así el sistema arranca "desde cero" sin que cada DAO
     * tenga que manejar ese caso por separado).
     */
    public static Document cargarOCrear(String rutaArchivo, String elementoRaiz) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File archivo = new File(rutaArchivo);

            if (archivo.exists()) {
                return builder.parse(archivo);
            }

            Document doc = builder.newDocument();
            doc.appendChild(doc.createElement(elementoRaiz));
            return doc;

        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar el archivo XML: " + rutaArchivo, e);
        }
    }

    /** Guarda el documento en disco, con indentación legible. */
    public static void guardar(Document doc, String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            File carpeta = archivo.getParentFile();
            if (carpeta != null && !carpeta.exists()) {
                carpeta.mkdirs();
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(doc), new StreamResult(archivo));

        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar el archivo XML: " + rutaArchivo, e);
        }
    }

    /** Devuelve los hijos DIRECTOS de padre cuyo tagName es nombreEtiqueta. */
    public static List<Element> hijos(Element padre, String nombreEtiqueta) {
        List<Element> resultado = new ArrayList<>();
        NodeList nodos = padre.getElementsByTagName(nombreEtiqueta);
        for (int i = 0; i < nodos.getLength(); i++) {
            Node nodo = nodos.item(i);
            if (nodo.getParentNode() == padre && nodo instanceof Element) {
                resultado.add((Element) nodo);
            }
        }
        return resultado;
    }

    /** Crea un elemento hijo con texto simple (ej: <id>5</id>) y lo agrega a padre. */
    public static Element agregarTexto(Document doc, Element padre, String etiqueta, String valor) {
        Element el = doc.createElement(etiqueta);
        el.setTextContent(valor == null ? "" : valor);
        padre.appendChild(el);
        return el;
    }

    /** Lee el texto del primer hijo con esa etiqueta, o "" si no existe. */
    public static String textoDe(Element padre, String etiqueta) {
        NodeList nodos = padre.getElementsByTagName(etiqueta);
        if (nodos.getLength() == 0) {
            return "";
        }
        return nodos.item(0).getTextContent();
    }
}