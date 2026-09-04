package sistema.reservas.dao;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Data.persistence.XmlUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación real de CategoriaRecursoDAO usando XML.
 * Archivo: data/categorias.xml (ver /docs/CONTRATO_XML.md).
 *
 * Regla de id autogenerado (fijada en el contrato): el nuevo id es
 * (máximo id existente) + 1, o 1 si todavía no hay categorías.
 */
public class CategoriaRecursoDAOXml implements CategoriaRecursoDAO {

    private static final String RUTA_ARCHIVO = "data/categorias.xml";
    private static final String RAIZ = "categorias";
    private static final String ITEM = "categoria";

    @Override
    public CategoriaRecurso buscarPorId(int id) {
        for (CategoriaRecurso c : listarTodos()) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    @Override
    public List<CategoriaRecurso> buscarPorDescripcion(String descripcion) {
        List<CategoriaRecurso> resultado = new ArrayList<>();
        for (CategoriaRecurso c : listarTodos()) {
            if (c.getDescripcion() != null
                    && c.getDescripcion().toLowerCase().contains(descripcion.toLowerCase())) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    @Override
    public void guardar(CategoriaRecurso categoria) {
        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);
        Element raiz = doc.getDocumentElement();

        categoria.setId(siguienteId(raiz));

        raiz.appendChild(categoriaAElement(doc, categoria));
        XmlUtil.guardar(doc, RUTA_ARCHIVO);
    }

    @Override
    public void actualizar(CategoriaRecurso categoria) {
        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);
        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            if (Integer.parseInt(XmlUtil.textoDe(item, "id")) == categoria.getId()) {
                raiz.removeChild(item);
                raiz.appendChild(categoriaAElement(doc, categoria));
                XmlUtil.guardar(doc, RUTA_ARCHIVO);
                return;
            }
        }
    }

    @Override
    public void eliminar(int id) {
        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);
        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            if (Integer.parseInt(XmlUtil.textoDe(item, "id")) == id) {
                raiz.removeChild(item);
                break;
            }
        }
        XmlUtil.guardar(doc, RUTA_ARCHIVO);
    }

    @Override
    public List<CategoriaRecurso> listarTodos() {
        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);
        Element raiz = doc.getDocumentElement();

        List<CategoriaRecurso> categorias = new ArrayList<>();
        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            int id = Integer.parseInt(XmlUtil.textoDe(item, "id"));
            String descripcion = XmlUtil.textoDe(item, "descripcion");
            categorias.add(new CategoriaRecurso(id, "", descripcion));
        }
        return categorias;
    }

    private int siguienteId(Element raiz) {
        int maximo = 0;
        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            int id = Integer.parseInt(XmlUtil.textoDe(item, "id"));
            if (id > maximo) {
                maximo = id;
            }
        }
        return maximo + 1;
    }

    private Element categoriaAElement(Document doc, CategoriaRecurso categoria) {
        Element item = doc.createElement(ITEM);
        XmlUtil.agregarTexto(doc, item, "id", String.valueOf(categoria.getId()));
        XmlUtil.agregarTexto(doc, item, "descripcion", categoria.getDescripcion());
        return item;
    }
}