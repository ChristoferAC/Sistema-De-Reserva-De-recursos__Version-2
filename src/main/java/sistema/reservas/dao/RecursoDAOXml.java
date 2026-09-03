package sistema.reservas.dao;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sistema.reservas.Categoria.CategoriaRecurso;
import sistema.reservas.Recurso.Recurso;
import sistema.reservas.persistence.XmlUtil;
import java.util.ArrayList;
import java.util.List;

public class RecursoDAOXml implements RecursoDAO {

    private static final String RUTA_ARCHIVO = "data/recursos.xml";

    private static final String RAIZ = "recursos";

    private static final String ITEM = "recurso";

    private final CategoriaRecursoDAO categoriaDAO = new CategoriaRecursoDAOXml();

    @Override
    public Recurso buscarPorId(String id) {
        if (id == null) {
            return null;
        }
        for (Recurso recurso : listar()) {
            if (id.equals(recurso.getId())) {
                return recurso;
            }
        }
        return null;
    }

    @Override
    public List<Recurso> listar() {

        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);
        Element raiz = doc.getDocumentElement();

        List<Recurso> recursos = new ArrayList<>();

        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            recursos.add(elementoARecurso(item));
        }
        return recursos;
    }

    @Override
    public List<Recurso> listarPorCategoria(int idCategoria) {
        List<Recurso> resultado = new ArrayList<>();
        for (Recurso recurso : listar()) {
            if (recurso.getCategoria() != null && recurso.getCategoria().getId() == idCategoria) {
                resultado.add(recurso);
            }
        }
        return resultado;
    }

    @Override
    public void guardar(Recurso recurso) {
        validarRecurso(recurso);
        if (buscarPorId(recurso.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un recurso con el ID: " + recurso.getId());
        }
        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);

        Element raiz = doc.getDocumentElement();
        raiz.appendChild(recursoAElemento(doc, recurso));

        XmlUtil.guardar(doc, RUTA_ARCHIVO);
    }

    @Override
    public void actualizar(Recurso recurso) {
        validarRecurso(recurso);

        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);

        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            String id = XmlUtil.textoDe(item, "id");
            if (recurso.getId().equals(id)) {
                raiz.removeChild(item);
                raiz.appendChild(recursoAElemento(doc, recurso));
                XmlUtil.guardar(doc, RUTA_ARCHIVO);
                return;
            }
        }
        throw new IllegalArgumentException("No existe un recurso con el ID: " + recurso.getId());
    }

    @Override
    public void eliminar(String id) {

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del recurso es obligatorio.");
        }

        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);

        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            String idActual = XmlUtil.textoDe(item, "id");

            if (id.equals(idActual)) {
                raiz.removeChild(item);
                XmlUtil.guardar(doc, RUTA_ARCHIVO);
                return;
            }
        }
        throw new IllegalArgumentException("No existe un recurso con el ID: " + id);
    }

    private Element recursoAElemento(Document doc, Recurso recurso) {

        Element item = doc.createElement(ITEM);

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

        int idCategoria = Integer.parseInt(textoIdCategoria);

        CategoriaRecurso categoria = categoriaDAO.buscarPorId(idCategoria);

        if (categoria == null) {
            throw new RuntimeException("La categoría " + idCategoria + " del recurso " + id + " no existe.");
        }

        return new Recurso(id, nombre, descripcion, categoria);
    }

    private void validarRecurso(Recurso recurso) {

        if (recurso == null) {
            throw new IllegalArgumentException("El recurso no puede ser nulo.");
        }

        if (recurso.getId() == null || recurso.getId().trim().isEmpty()) {

            throw new IllegalArgumentException("El ID del recurso es obligatorio.");
        }

        if (recurso.getCategoria() == null) {

            throw new IllegalArgumentException("El recurso debe tener una categoría.");
        }
    }
}
