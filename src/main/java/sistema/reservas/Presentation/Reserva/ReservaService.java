// ReservaService.java
package sistema.reservas.Presentation.Reserva;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sistema.reservas.Data.persistence.XmlUtil;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Presentation.Recurso.RecursoService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaService {

    private static final String RUTA_RESERVAS = "data/reservas.xml";
    private static final String RUTA_USUARIOS = "data/usuarios.xml";
    private static final String RUTA_CATEGORIAS = "data/categorias.xml";

    private static final String RAIZ_RESERVAS = "reservas";
    private static final String RAIZ_USUARIOS = "usuarios";
    private static final String RAIZ_CATEGORIAS = "categorias";

    private static final String ITEM_RESERVA = "reserva";
    private static final String ITEM_USUARIO = "usuario";
    private static final String ITEM_CATEGORIA = "categoria";

    private final RecursoService recursoService;

    public ReservaService() {
        this(new RecursoService());
    }

    public ReservaService(RecursoService recursoService) {
        if (recursoService == null) {
            throw new IllegalArgumentException("El RecursoService no puede ser nulo.");
        }
        this.recursoService = recursoService;
    }

    public void crearReserva(Reserva reserva) {
        validarReserva(reserva);

        if (buscarReserva(reserva.getId()) != null) {
            throw new IllegalArgumentException("Ya existe una reserva con el ID indicado.");
        }

        List<CategoriaRecurso> categoriasSinDisponibilidad = obtenerCategoriasSinDisponibilidad(reserva, -1);

        if (!categoriasSinDisponibilidad.isEmpty()) {
            throw new IllegalArgumentException(construirMensajeDisponibilidad(categoriasSinDisponibilidad));
        }
        asignarRecursosDisponibles(reserva, -1);
        reserva.setEstado(Reserva.Estado.ACTIVA);
        guardarReserva(reserva);
    }

    public Reserva buscarReserva(int id) {
        Document doc = XmlUtil.cargarOCrear(RUTA_RESERVAS, RAIZ_RESERVAS);

        Element raiz = doc.getDocumentElement();
        for (Element item : XmlUtil.hijos(raiz, ITEM_RESERVA)) {
            int idActual = parsearEntero(XmlUtil.textoDe(item, "id"), "ID de reserva inválido");

            if (idActual == id) {
                return elementoAReserva(item);
            }
        }
        return null;
    }

    public List<Reserva> listarReservas() {
        Document doc = XmlUtil.cargarOCrear(RUTA_RESERVAS, RAIZ_RESERVAS);
        Element raiz = doc.getDocumentElement();
        List<Reserva> reservas = new ArrayList<>();
        for (Element item : XmlUtil.hijos(raiz, ITEM_RESERVA)) {
            reservas.add(elementoAReserva(item));
        }
        return reservas;
    }

    public List<Reserva> listarReservasFuncionario(int idFuncionario) {
        if (idFuncionario <= 0) {
            throw new IllegalArgumentException("El ID del funcionario debe ser válido.");
        }

        List<Reserva> resultado = new ArrayList<>();

        for (Reserva reserva : listarReservas()) {
            if (reserva.getFuncionario() != null && reserva.getFuncionario().getId() == idFuncionario) {
                resultado.add(reserva);
            }
        }
        return resultado;
    }

    public List<CategoriaRecurso> obtenerCategoriasSinDisponibilidad(Reserva reserva, int idReservaExcluida) {
        validarDatosDeDisponibilidad(reserva);
        List<CategoriaRecurso> resultado = new ArrayList<>();
        List<Reserva> reservasDelDia = listarReservasPorFecha(reserva.getFecha());

        for (CategoriaRecurso categoria : reserva.getCategoriasSolicitadas()) {
            boolean disponible = false;
            List<Recurso> recursos = recursoService.listarPorCategoria(categoria.getId());

            for (Recurso recurso : recursos) {
                if (recursoDisponible(recurso, reserva, reservasDelDia, idReservaExcluida)) {
                    disponible = true;
                    break;
                }
            }
            if (!disponible) {
                resultado.add(categoria);
            }
        }
        return resultado;
    }

    public boolean hayDisponibilidad(Reserva reserva) {
        validarDatosDeDisponibilidad(reserva);
        return obtenerCategoriasSinDisponibilidad(reserva, -1).isEmpty();
    }

    public void modificarReserva(Reserva reserva) {
        validarReserva(reserva);

        Reserva existente = buscarReserva(reserva.getId());
        if (existente == null) {
            throw new IllegalArgumentException("La reserva no existe.");
        }
        if (!existente.isActiva()) {
            throw new IllegalArgumentException("No se puede modificar una reserva cancelada.");
        }

        List<CategoriaRecurso> categoriasSinDisponibilidad = obtenerCategoriasSinDisponibilidad(reserva, reserva.getId());

        if (!categoriasSinDisponibilidad.isEmpty()) {
            throw new IllegalArgumentException(construirMensajeDisponibilidad(categoriasSinDisponibilidad));
        }
        asignarRecursosDisponibles(reserva, reserva.getId());
        reserva.setEstado(Reserva.Estado.ACTIVA);
        actualizarReserva(reserva);
    }

    /**
     * Cancela una reserva futura: libera sus recursos y la marca
     * CANCELADA, conservándola en el historial (ya NO se borra del XML)
     * para que siga apareciendo en "Mis reservas" con su estado, tal
     * como pide el enunciado.
     */
    public void cancelarReserva(int id) {
        Reserva reserva = buscarReserva(id);

        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no existe.");
        }
        if (!reserva.isActiva()) {
            throw new IllegalArgumentException("La reserva ya se encuentra cancelada.");
        }
        if (reserva.getFecha().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Solo se pueden cancelar reservas futuras.");
        }

        reserva.limpiarRecursosAsignados();
        reserva.setEstado(Reserva.Estado.CANCELADA);

        actualizarReserva(reserva);
    }

    private List<Reserva> listarReservasPorFecha(LocalDate fecha) {
        List<Reserva> resultado = new ArrayList<>();

        if (fecha == null) {
            return resultado;
        }

        for (Reserva reserva : listarReservas()) {
            if (fecha.equals(reserva.getFecha())) {
                resultado.add(reserva);
            }
        }

        return resultado;
    }

    private void guardarReserva(Reserva reserva) {
        Document doc = XmlUtil.cargarOCrear(RUTA_RESERVAS, RAIZ_RESERVAS);
        Element raiz = doc.getDocumentElement();

        raiz.appendChild(reservaAElemento(doc, reserva));

        XmlUtil.guardar(doc, RUTA_RESERVAS);
    }

    private void actualizarReserva(Reserva reserva) {
        Document doc = XmlUtil.cargarOCrear(RUTA_RESERVAS, RAIZ_RESERVAS);

        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM_RESERVA)) {
            int idActual = parsearEntero(XmlUtil.textoDe(item, "id"), "ID de reserva inválido");

            if (idActual == reserva.getId()) {

                raiz.removeChild(item);

                raiz.appendChild(reservaAElemento(doc, reserva));

                XmlUtil.guardar(doc, RUTA_RESERVAS);
                return;
            }
        }

        throw new IllegalArgumentException("La reserva no existe.");
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

        validarDatosDeDisponibilidad(reserva);
        validarCategorias(reserva.getCategoriasSolicitadas());
    }

    private void validarCategorias(List<CategoriaRecurso> categorias) {
        for (int i = 0; i < categorias.size(); i++) {

            CategoriaRecurso categoria = categorias.get(i);

            if (categoria == null) {
                throw new IllegalArgumentException("La reserva no puede contener categorías nulas.");
            }

            for (int j = i + 1; j < categorias.size(); j++) {
                CategoriaRecurso otraCategoria = categorias.get(j);
                if (otraCategoria != null && categoria.getId() == otraCategoria.getId()) {
                    throw new IllegalArgumentException("La categoría '" + nombreCategoria(categoria) + "' fue solicitada más de una vez.");
                }
            }
        }
    }

    private void validarDatosDeDisponibilidad(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }

        if (reserva.getFecha() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }

        if (reserva.getHoraInicio() == null || reserva.getHoraFin() == null) {
            throw new IllegalArgumentException("Las horas de inicio y finalización son obligatorias.");
        }

        if (!reserva.getHoraInicio().isBefore(reserva.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior " + "a la hora de finalización.");
        }

        if (reserva.getCategoriasSolicitadas() == null || reserva.getCategoriasSolicitadas().isEmpty()) {
            throw new IllegalArgumentException("Debe solicitar al menos una categoría.");
        }
    }

    private void asignarRecursosDisponibles(Reserva reserva, int idReservaExcluida) {
        List<Reserva> reservasDelDia = listarReservasPorFecha(reserva.getFecha());

        List<Recurso> nuevosRecursos = new ArrayList<>();

        for (CategoriaRecurso categoria : reserva.getCategoriasSolicitadas()) {
            List<Recurso> recursos = recursoService.listarPorCategoria(categoria.getId());

            boolean encontrado = false;
            for (Recurso recurso : recursos) {
                if (recursoDisponible(recurso, reserva, reservasDelDia, idReservaExcluida)) {
                    nuevosRecursos.add(recurso);
                    encontrado = true;
                    break;
                }
            }

            if (!encontrado) {
                throw new IllegalArgumentException("No hay recursos disponibles para la categoría: " + nombreCategoria(categoria));
            }
        }

        reserva.limpiarRecursosAsignados();
        for (Recurso recurso : nuevosRecursos) {
            reserva.agregarRecurso(recurso);
        }
    }

    private boolean recursoDisponible(Recurso recurso, Reserva reserva, List<Reserva> reservasDelDia, int idReservaExcluida) {
        if (recurso == null) {
            return false;
        }

        for (Reserva otraReserva : reservasDelDia) {
            if (otraReserva == null || otraReserva.getId() == idReservaExcluida) {
                continue;
            }
            // Una reserva CANCELADA ya no ocupa el recurso.
            if (!otraReserva.isActiva()) {
                continue;
            }

            if (tieneRecurso(otraReserva, recurso.getId()) && haySolapamiento(reserva, otraReserva)) {
                return false;
            }
        }
        return true;
    }

    private boolean tieneRecurso(Reserva reserva, String idRecurso) {
        if (reserva.getRecursosAsignados() == null) {
            return false;
        }

        for (Recurso recurso : reserva.getRecursosAsignados()) {
            if (recurso != null && idRecurso.equals(recurso.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean haySolapamiento(Reserva reserva1, Reserva reserva2) {
        if (reserva1.getFecha() == null || reserva2.getFecha() == null) {
            return false;
        }

        if (!reserva1.getFecha().equals(reserva2.getFecha())) {
            return false;
        }
        return reserva1.getHoraInicio().isBefore(reserva2.getHoraFin()) && reserva2.getHoraInicio().isBefore(reserva1.getHoraFin());
    }

    private Element reservaAElemento(Document doc, Reserva reserva) {
        Element item = doc.createElement(ITEM_RESERVA);

        XmlUtil.agregarTexto(doc, item, "id", String.valueOf(reserva.getId()));
        XmlUtil.agregarTexto(doc, item, "idFuncionario", String.valueOf(reserva.getFuncionario().getId()));

        XmlUtil.agregarTexto(doc, item, "actividad", reserva.getActividad());

        XmlUtil.agregarTexto(doc, item, "fecha", reserva.getFecha().toString());

        XmlUtil.agregarTexto(doc, item, "horaInicio", reserva.getHoraInicio().toString());

        XmlUtil.agregarTexto(doc, item, "horaFin", reserva.getHoraFin().toString());

        XmlUtil.agregarTexto(doc, item, "estado", reserva.getEstado().name());

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
        int id = parsearEntero(XmlUtil.textoDe(item, "id"), "ID de reserva inválido");

        int idFuncionario = parsearEntero(XmlUtil.textoDe(item, "idFuncionario"), "ID de funcionario inválido");

        Funcionario funcionario = buscarFuncionarioPorId(idFuncionario);

        if (funcionario == null) {
            throw new IllegalArgumentException("El funcionario " + idFuncionario + " de la reserva " + id + " no existe.");
        }

        String actividad = XmlUtil.textoDe(item, "actividad");

        LocalDate fecha = LocalDate.parse(XmlUtil.textoDe(item, "fecha"));

        LocalTime horaInicio = LocalTime.parse(XmlUtil.textoDe(item, "horaInicio"));

        LocalTime horaFin = LocalTime.parse(XmlUtil.textoDe(item, "horaFin"));

        Reserva reserva = new Reserva(id, funcionario, actividad, fecha, horaInicio, horaFin);

        String textoEstado = XmlUtil.textoDe(item, "estado");
        if (textoEstado != null && !textoEstado.isBlank()) {
            reserva.setEstado(Reserva.Estado.valueOf(textoEstado));
        } else {
            // Compatibilidad con reservas XML antiguas que no tenían estado.
            reserva.setEstado(Reserva.Estado.ACTIVA);
        }

        for (Element contenedor : XmlUtil.hijos(item, "categoriasSolicitadas")) {
            for (Element categoriaElemento : XmlUtil.hijos(contenedor, "categoria")) {
                int idCategoria = parsearEntero(XmlUtil.textoDe(categoriaElemento, "id"), "ID de categoría inválido");

                CategoriaRecurso categoria = buscarCategoriaPorId(idCategoria);

                if (categoria != null) {
                    reserva.agregarCategoria(categoria);
                }
            }
        }

        for (Element contenedor : XmlUtil.hijos(item, "recursosAsignados")) {
            for (Element recursoElemento : XmlUtil.hijos(contenedor, "recurso")) {

                String idRecurso = XmlUtil.textoDe(recursoElemento, "id");
                Recurso recurso = recursoService.buscarRecurso(idRecurso);
                if (recurso != null) {
                    reserva.agregarRecurso(recurso);
                }
            }
        }
        return reserva;
    }

    private Funcionario buscarFuncionarioPorId(int id) {
        Document doc = XmlUtil.cargarOCrear(RUTA_USUARIOS, RAIZ_USUARIOS);

        Element raiz = doc.getDocumentElement();

        for (Element item : XmlUtil.hijos(raiz, ITEM_USUARIO)) {

            String tipo = item.getAttribute("tipo");

            String textoId = XmlUtil.textoDe(item, "id");

            if (!"FUNCIONARIO".equals(tipo) || textoId == null || textoId.trim().isEmpty()) {
                continue;
            }
            try {
                if (Integer.parseInt(textoId.trim()) != id) {
                    continue;
                }
            } catch (NumberFormatException e) {
                continue;
            }

            return new Funcionario(id, XmlUtil.textoDe(item, "nombre"), XmlUtil.textoDe(item, "username"), XmlUtil.textoDe(item, "password"), XmlUtil.textoDe(item, "telefono"));
        }

        return null;
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

    private String construirMensajeDisponibilidad(List<CategoriaRecurso> categorias) {

        StringBuilder mensaje = new StringBuilder("No hay disponibilidad para: ");

        for (int i = 0; i < categorias.size(); i++) {

            mensaje.append(nombreCategoria(categorias.get(i)));

            if (i < categorias.size() - 1) {
                mensaje.append(", ");
            }
        }

        mensaje.append(".");

        return mensaje.toString();
    }

    private String nombreCategoria(CategoriaRecurso categoria) {
        if (categoria == null) {
            return "categoría";
        }

        if (categoria.getNombre() != null && !categoria.getNombre().trim().isEmpty()) {
            return categoria.getNombre();
        }

        if (categoria.getDescripcion() != null && !categoria.getDescripcion().trim().isEmpty()) {
            return categoria.getDescripcion();
        }

        return String.valueOf(categoria.getId());
    }

    private int parsearEntero(String valor, String mensaje) {

        try {
            return Integer.parseInt(valor.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(mensaje + ".");
        }
    }
}