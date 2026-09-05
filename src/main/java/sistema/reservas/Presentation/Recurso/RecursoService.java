// RecursoService.java
package sistema.reservas.Presentation.Recurso;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sistema.reservas.Data.persistence.XmlUtil;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;

import java.util.ArrayList;
import java.util.List;

public class RecursoService {

    private static final String RUTA_RECURSOS = "data/recursos.xml";
    private static final String RUTA_CATEGORIAS = "data/categorias.xml";

    private static final String RAIZ_RECURSOS = "recursos";
    private static final String RAIZ_CATEGORIAS = "categorias";

    private static final String ITEM_RECURSO = "recurso";
    private static final String ITEM_CATEGORIA = "categoria";

    public RecursoService() {
    }

    public void crearRecurso(Recurso recurso) {
        validarRecurso(recurso);

        if (buscarRecursoInterno(recurso.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un recurso con el ID indicado.");
        }

        Document doc = XmlUtil.cargarOCrear(RUTA_RECURSOS, RAIZ_RECURSOS);
        Element raiz = doc.getDocumentElement();

        raiz.appendChild(recursoAElemento(doc, recurso));

        XmlUtil.guardar(doc, RUTA_RECURSOS);
    }

    public Recurso buscarRecurso(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El ID del recurso es obligatorio.");
        }

        return buscarRecursoInterno(id);
    }

    private Recurso buscarRecursoInterno(String id) {
        if (id == null) {
            return null;
        }

        for (Recurso recurso : listarRecursos()) {
            if (id.equals(recurso.getId())) {
                return recurso;
            }
        }

        return null;
    }

    public List<Recurso> listarRecursos() {
        Document doc = XmlUtil.cargarOCrear(RUTA_RECURSOS, RAIZ_RECURSOS);
        Element raiz = doc.getDocumentElement();

        List<Recurso> recursos = new ArrayList<>();

        for (Element item : XmlUtil.hijos(raiz, ITEM_RECURSO)) {
            recursos.add(elementoARecurso(item));
        }

        return recursos;
    }

    public List<Recurso> listarPorCategoria(int idCategoria) {
        List<Recurso> resultado = new ArrayList<>();

        for (Recurso recurso : listarRecursos()) {
            if (recurso.getCategoria() != null && recurso.getCategoria().getId() == idCategoria) {
                resultado.add(recurso);
            }
        }

        return resultado;
    }

    public void modificarRecurso(Recurso recurso) {
        validarRecurso(recurso);

        Document doc = XmlUtil.cargarOCrear(RUTA_RECURSOS, RAIZ_RECURSOS);
        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM_RECURSO)) {
            String idActual = XmlUtil.textoDe(item, "id");

            if (recurso.getId().equals(idActual)) {
                raiz.removeChild(item);
                raiz.appendChild(recursoAElemento(doc, recurso));

                XmlUtil.guardar(doc, RUTA_RECURSOS);
                return;
            }
        }

        throw new IllegalArgumentException("El recurso no existe.");
    }

    public void eliminarRecurso(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del recurso es obligatorio.");
        }

        Document doc = XmlUtil.cargarOCrear(RUTA_RECURSOS, RAIZ_RECURSOS);
        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM_RECURSO)) {
            if (id.equals(XmlUtil.textoDe(item, "id"))) {
                raiz.removeChild(item);
                XmlUtil.guardar(doc, RUTA_RECURSOS);
                return;
            }
        }

        throw new IllegalArgumentException("El recurso no existe.");
    }

    private Element recursoAElemento(Document doc, Recurso recurso) {
        Element item = doc.createElement(ITEM_RECURSO);

        XmlUtil.agregarTexto(doc, item, "id", recurso.getId());

        XmlUtil.agregarTexto(doc, item, "nombre", recurso.getNombre());

        XmlUtil.agregarTexto(doc, item, "descripcion", recurso.getDescripcion());

        XmlUtil.agregarTexto(doc, item, "idCategoria", String.valueOf(recurso.getCategoria().getId()));

        return item;
    }

    private Recurso elementoARecurso(Element item) {
        String id = XmlUtil.textoDe(item, "id");
        String nombre = XmlUtil.textoDe(item, "nombre");
        String descripcion = XmlUtil.textoDe(item, "descripcion");
        String textoIdCategoria = XmlUtil.textoDe(item, "idCategoria");

        if (textoIdCategoria == null || textoIdCategoria.trim().isEmpty()) {
            throw new IllegalArgumentException("El recurso " + id + " no tiene categoría.");
        }

        int idCategoria;

        try {
            idCategoria = Integer.parseInt(textoIdCategoria.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La categoría del recurso " + id + " no es válida.");
        }

        CategoriaRecurso categoria = buscarCategoriaPorId(idCategoria);

        if (categoria == null) {
            throw new IllegalArgumentException("La categoría " + idCategoria + " del recurso " + id + " no existe.");
        }

        return new Recurso(id, nombre, descripcion, categoria);
    }

    private CategoriaRecurso buscarCategoriaPorId(int id) {
        Document doc = XmlUtil.cargarOCrear(RUTA_CATEGORIAS, RAIZ_CATEGORIAS);

        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM_CATEGORIA)) {
            String textoId = XmlUtil.textoDe(item, "id");

            if (textoId == null || textoId.trim().isEmpty()) {
                continue;
            }

            try {
                if (Integer.parseInt(textoId.trim()) != id) {
                    continue;
                }
            } catch (NumberFormatException e) {
                continue;
            }

            String nombre = XmlUtil.textoDe(item, "nombre");
            String descripcion = XmlUtil.textoDe(item, "descripcion");

            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = descripcion;
            }

            return new CategoriaRecurso(id, nombre, descripcion);
        }
        return null;
    }

    private void validarRecurso(Recurso recurso) {
        if (recurso == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo.");
        }

        if (recurso.getId() == null || recurso.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del recurso es obligatorio.");
        }

        if (recurso.getNombre() == null || recurso.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del recurso es obligatorio.");
        }

        if (recurso.getDescripcion() == null || recurso.getDescripcion().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del recurso es obligatoria.");
        }

        if (recurso.getCategoria() == null) {
            throw new IllegalArgumentException("El recurso debe tener una categoría.");
        }
    }
}