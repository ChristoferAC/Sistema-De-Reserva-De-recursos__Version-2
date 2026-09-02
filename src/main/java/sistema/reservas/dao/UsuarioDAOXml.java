package sistema.reservas.dao;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sistema.reservas.model.Administrador;
import sistema.reservas.model.Funcionario;
import sistema.reservas.model.Usuario;
import sistema.reservas.persistence.XmlUtil;

import java.util.ArrayList;
import java.util.List;


public class UsuarioDAOXml implements UsuarioDAO {

    private static final String RUTA_ARCHIVO = "data/usuarios.xml";
    private static final String RAIZ = "usuarios";
    private static final String ITEM = "usuario";

    @Override
    public Usuario buscarPorId(int id) {
        for (Usuario u : listarTodos()) {
            if (u.getId() == id) {
                return u;
            }
        }
        return null;
    }

    @Override
    public Usuario buscarPorUsername(String username) {
        for (Usuario u : listarTodos()) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    @Override
    public void guardar(Usuario usuario) {
        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);
        Element raiz = doc.getDocumentElement();
        raiz.appendChild(usuarioAElement(doc, usuario));
        XmlUtil.guardar(doc, RUTA_ARCHIVO);
    }

    @Override
    public void actualizar(Usuario usuario) {
        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);
        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            if (Integer.parseInt(XmlUtil.textoDe(item, "id")) == usuario.getId()) {
                raiz.removeChild(item);
                raiz.appendChild(usuarioAElement(doc, usuario));
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
    public List<Usuario> listarTodos() {
        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);
        Element raiz = doc.getDocumentElement();

        List<Usuario> usuarios = new ArrayList<>();
        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            usuarios.add(elementAUsuario(item));
        }
        return usuarios;
    }

    // ---- mapeo Usuario <-> XML ----

    private Element usuarioAElement(Document doc, Usuario usuario) {
        Element item = doc.createElement(ITEM);
        item.setAttribute("tipo", usuario.getRol()); // "ADMIN" o "FUNCIONARIO"

        XmlUtil.agregarTexto(doc, item, "id", String.valueOf(usuario.getId()));
        XmlUtil.agregarTexto(doc, item, "nombre", usuario.getNombre());
        XmlUtil.agregarTexto(doc, item, "username", usuario.getUsername());
        XmlUtil.agregarTexto(doc, item, "password", usuario.getPassword());

        if (usuario instanceof Funcionario) {
            XmlUtil.agregarTexto(doc, item, "telefono", ((Funcionario) usuario).getTelefono());
        }
        return item;
    }

    private Usuario elementAUsuario(Element item) {
        String tipo = item.getAttribute("tipo");
        int id = Integer.parseInt(XmlUtil.textoDe(item, "id"));
        String nombre = XmlUtil.textoDe(item, "nombre");
        String username = XmlUtil.textoDe(item, "username");
        String password = XmlUtil.textoDe(item, "password");

        if ("ADMIN".equals(tipo)) {
            return new Administrador(id, nombre, username, password);
        }

        String telefono = XmlUtil.textoDe(item, "telefono");
        return new Funcionario(id, nombre, username, password, telefono);
    }
}