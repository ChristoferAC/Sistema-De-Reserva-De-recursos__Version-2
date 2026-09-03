package sistema.reservas.dao;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sistema.reservas.Categoria.CategoriaRecurso;
import sistema.reservas.Funcionario.Funcionario;
import sistema.reservas.Recurso.Recurso;
import sistema.reservas.Reserva.Reserva;
import sistema.reservas.persistence.XmlUtil;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAOXml implements ReservaDAO {
    private static final String RUTA_ARCHIVO = "data/reservas.xml";

    private static final String RAIZ = "reservas";

    private static final String ITEM = "reserva";

    private final FuncionarioDAO funcionarioDAO = new FuncionarioDAOXml();

    private final CategoriaRecursoDAO categoriaDAO = new CategoriaRecursoDAOXml();

    private final RecursoDAO recursoDAO = new RecursoDAOXml();

    @Override
    public Reserva buscarPorId(int id) {
        for (Reserva reserva : listar()) {
            if (reserva.getId() == id) {
                return reserva;
            }
        }
        return null;
    }

    @Override
    public List<Reserva> listar() {
        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);

        Element raiz = doc.getDocumentElement();

        List<Reserva> reservas = new ArrayList<>();

        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            reservas.add(elementoAReserva(item));
        }
        return reservas;
    }

    @Override
    public List<Reserva>
    listarPorFuncionario(int idFuncionario) {

        List<Reserva> resultado = new ArrayList<>();

        for (Reserva reserva : listar()) {
            if (reserva.getFuncionario() != null && reserva.getFuncionario().getId() == idFuncionario) {
                resultado.add(reserva);
            }
        }
        return resultado;
    }

    @Override
    public List<Reserva> listarPorFecha(String fecha) {

        List<Reserva> resultado = new ArrayList<>();

        if (fecha == null) {
            return resultado;
        }

        for (Reserva reserva : listar()) {
            if (reserva.getFecha() != null && reserva.getFecha().toString().equals(fecha)) {
                resultado.add(reserva);
            }
        }

        return resultado;
    }

    @Override
    public void guardar(Reserva reserva) {
        validarReserva(reserva);

        if (buscarPorId(reserva.getId()) != null) {
            throw new IllegalArgumentException("Ya existe una reserva con el ID: " + reserva.getId());
        }
        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);

        Element raiz = doc.getDocumentElement();

        raiz.appendChild(reservaAElemento(doc, reserva));

        XmlUtil.guardar(doc, RUTA_ARCHIVO);
    }

    @Override
    public void actualizar(Reserva reserva) {
        validarReserva(reserva);

        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);

        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            int idActual = Integer.parseInt(XmlUtil.textoDe(item, "id"));
            if (idActual == reserva.getId()) {
                raiz.removeChild(item);
                raiz.appendChild(reservaAElemento(doc, reserva));
                XmlUtil.guardar(doc, RUTA_ARCHIVO);
                return;
            }
        }
        throw new IllegalArgumentException("No existe la reserva con ID: " + reserva.getId());
    }

    @Override
    public void eliminar(int id) {

        Document doc = XmlUtil.cargarOCrear(RUTA_ARCHIVO, RAIZ);

        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM)) {
            int idActual = Integer.parseInt(XmlUtil.textoDe(item, "id"));

            if (idActual == id) {
                raiz.removeChild(item);
                XmlUtil.guardar(doc, RUTA_ARCHIVO);
                return;
            }
        }

        throw new IllegalArgumentException("No existe la reserva con ID: " + id);
    }

    private Element reservaAElemento(Document doc, Reserva reserva) {

        Element item = doc.createElement(ITEM);

        XmlUtil.agregarTexto(doc, item, "id", String.valueOf(reserva.getId()));

        XmlUtil.agregarTexto(doc, item, "idFuncionario", String.valueOf(reserva.getFuncionario().getId()));

        XmlUtil.agregarTexto(doc, item, "actividad", reserva.getActividad());

        XmlUtil.agregarTexto(doc, item, "fecha", reserva.getFecha().toString());

        XmlUtil.agregarTexto(doc, item, "horaInicio", reserva.getHoraInicio().toString());

        XmlUtil.agregarTexto(doc, item, "horaFin", reserva.getHoraFin().toString());



        Element categorias = doc.createElement("categoriasSolicitadas");

        for (CategoriaRecurso categoria : reserva.getCategoriasSolicitadas()) {
            Element categoriaElemento = doc.createElement("categoria");

            XmlUtil.agregarTexto(doc, categoriaElemento, "id", String.valueOf(categoria.getId()));

            categorias.appendChild(categoriaElemento);
        }

        item.appendChild(categorias);



        Element recursos = doc.createElement("recursosAsignados");

        for (Recurso recurso : reserva.getRecursosAsignados()) {
            Element recursoElemento = doc.createElement("recurso");

            XmlUtil.agregarTexto(doc, recursoElemento, "id", recurso.getId());

            recursos.appendChild(recursoElemento);
        }

        item.appendChild(recursos);
        return item;
    }

    private Reserva elementoAReserva(Element item) {

        int id = Integer.parseInt(XmlUtil.textoDe(item, "id"));

        int idFuncionario = Integer.parseInt(XmlUtil.textoDe(item, "idFuncionario"));

        Funcionario funcionario = funcionarioDAO.buscarPorId(idFuncionario);

        if (funcionario == null) {
            throw new RuntimeException("El funcionario " + idFuncionario + " de la reserva " + id + " no existe.");
        }

        String actividad = XmlUtil.textoDe(item, "actividad");

        LocalDate fecha = LocalDate.parse(XmlUtil.textoDe(item, "fecha"));

        LocalTime horaInicio = LocalTime.parse(XmlUtil.textoDe(item, "horaInicio"));

        LocalTime horaFin = LocalTime.parse(XmlUtil.textoDe(item, "horaFin"));

        Reserva reserva = new Reserva(id, funcionario, actividad, fecha, horaInicio, horaFin);


        for (Element categoriaElemento : XmlUtil.hijos(item, "categoria")) {
            if (!"categoriasSolicitadas".equals(categoriaElemento.getParentNode().getNodeName())) {
                continue;
            }

            int idCategoria = Integer.parseInt(XmlUtil.textoDe(categoriaElemento, "id"));

            CategoriaRecurso categoria = categoriaDAO.buscarPorId(idCategoria);

            if (categoria != null) {
                reserva.agregarCategoria(categoria);
            }
        }


        for (Element recursoElemento : XmlUtil.hijos(item, "recurso")) {
            if (!"recursosAsignados".equals(recursoElemento.getParentNode().getNodeName())) {
                continue;
            }

            String idRecurso = XmlUtil.textoDe(recursoElemento, "id");

            Recurso recurso = recursoDAO.buscarPorId(idRecurso);

            if (recurso != null) {reserva.agregarRecurso(recurso);
            }
        }
        return reserva;
    }


    private void validarReserva(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }

        if (reserva.getFuncionario() == null) {
            throw new IllegalArgumentException("La reserva debe tener un funcionario.");
        }

        if (reserva.getActividad() == null || reserva.getActividad().trim().isEmpty()) {
            throw new IllegalArgumentException("La actividad es obligatoria.");
        }

        if (reserva.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        if (reserva.getHoraInicio() == null || reserva.getHoraFin() == null) {
            throw new IllegalArgumentException("Las horas son obligatorias.");
        }

        if (!reserva.getHoraInicio().isBefore(reserva.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior " + "a la hora final.");
        }

        if (reserva.getCategoriasSolicitadas() == null || reserva.getCategoriasSolicitadas().isEmpty()) {
            throw new IllegalArgumentException("La reserva debe tener al menos " + "una categoría.");
        }
    }
}