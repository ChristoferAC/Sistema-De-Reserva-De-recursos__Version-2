package sistema.reservas.Logic.Services;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import sistema.reservas.Data.llm.ReservaExtraccion;
import sistema.reservas.Data.llm.ReservaExtractorService;
import sistema.reservas.Data.persistence.RecursoDAO;
import sistema.reservas.Data.persistence.ReservaDAO;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Una instancia por Worker (sus DAO usan la conexión de ese Worker)
 *  El candado es UNO SOLO para todo el proceso.
 */
public class ServiceReserva implements IServiceReserva {

    private static final Object LOCK_RESERVAS = new Object();
    private static final int SIN_EXCLUSION = -1;
    private static final Logger LOG = Logger.getLogger(ServiceReserva.class.getName());

    private final ReservaDAO reservaDAO;
    private final RecursoDAO recursoDAO;
    private final Supplier<List<CategoriaRecurso>> proveedorCategorias;
    private final Notificador notificador;
    private final Object origen;

    public ServiceReserva(ReservaDAO reservaDAO, RecursoDAO recursoDAO,
                          Supplier<List<CategoriaRecurso>> proveedorCategorias,
                          Notificador notificador, Object origen) {
        if (reservaDAO == null || recursoDAO == null) {
            throw new IllegalArgumentException("Los DAO no pueden ser nulos.");
        }
        this.reservaDAO = reservaDAO;
        this.recursoDAO = recursoDAO;
        this.proveedorCategorias = proveedorCategorias != null ? proveedorCategorias : Collections::emptyList;
        this.notificador = notificador;
        this.origen = origen;
    }

    /** Para UtilitiesTEst con DAO falso (sin notificador ni categorías). */
    public ServiceReserva(ReservaDAO reservaDAO, RecursoDAO recursoDAO) {
        this(reservaDAO, recursoDAO, null, null, null);
    }

    // ------------------------------- crear -------------------------------

    @Override
    public Reserva crearReserva(Reserva reserva) {
        validarReserva(reserva);

        int idNuevo;
        synchronized (LOCK_RESERVAS) {
            List<Recurso> asignados = asignarRecursos(reserva, SIN_EXCLUSION);
            reserva.limpiarRecursosAsignados();
            for (Recurso recurso : asignados) {
                reserva.agregarRecurso(recurso);
            }
            reserva.setEstado(Reserva.Estado.ACTIVA);
            idNuevo = reservaDAO.insertar(reserva).getId();   // el commit ocurre dentro del DAO
        }

        Reserva creada = reservaDAO.buscarPorId(idNuevo);
        if (creada == null) {
            creada = reserva;
        }
        notificar(creada);
        return creada;
    }

    private void notificar(Reserva reserva) {
        if (notificador == null) {
            return;
        }
        try {
            notificador.nuevaReserva(reserva, origen);
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "No se pudo notificar la reserva " + reserva.getId(), e);
        }
    }

    // ----------------------------- consultas -----------------------------

    @Override
    public Reserva buscarReserva(int id) {
        return reservaDAO.buscarPorId(id);
    }

    @Override
    public List<Reserva> listarReservas() {
        return reservaDAO.listar();
    }

    @Override
    public List<Reserva> listarReservasFuncionario(int idFuncionario) {
        if (idFuncionario <= 0) {
            throw new IllegalArgumentException("El ID del funcionario debe ser válido.");
        }
        return reservaDAO.listarPorFuncionario(idFuncionario);
    }

    @Override
    public boolean hayDisponibilidad(Reserva reserva) {
        validarDatosDisponibilidad(reserva);
        try {
            asignarRecursos(reserva, SIN_EXCLUSION);
            return true;
        } catch (SinDisponibilidadException e) {
            return false;
        }
    }

    // ------------------------ modificar y cancelar ------------------------

    @Override
    public Reserva modificarReserva(Reserva reserva) {
        validarReserva(reserva);

        synchronized (LOCK_RESERVAS) {
            Reserva existente = reservaDAO.buscarPorId(reserva.getId());
            if (existente == null) {
                throw new IllegalArgumentException("La reserva no existe.");
            }
            if (!existente.isActiva()) {
                throw new IllegalArgumentException("No se puede modificar una reserva cancelada.");
            }

            List<Recurso> asignados = asignarRecursos(reserva, reserva.getId());
            reserva.setFuncionario(existente.getFuncionario());   // no se reasigna a otro funcionario
            reserva.limpiarRecursosAsignados();
            for (Recurso recurso : asignados) {
                reserva.agregarRecurso(recurso);
            }
            reserva.setEstado(Reserva.Estado.ACTIVA);
            reservaDAO.actualizar(reserva);
        }
        return reservaDAO.buscarPorId(reserva.getId());
    }

    /** Cancelar = cambiar el estado y liberar los recursos; */
    @Override
    public void cancelarReserva(int id) {
        synchronized (LOCK_RESERVAS) {
            Reserva reserva = reservaDAO.buscarPorId(id);
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
            reservaDAO.actualizar(reserva);
        }
    }

    // --------------------- disponibilidad y asignación ---------------------

    private List<Recurso> asignarRecursos(Reserva reserva, int idExcluida) {
        List<Reserva> activasDelDia = reservasActivasDelDia(reserva.getFecha());
        List<Recurso> asignados = new ArrayList<>();
        List<CategoriaRecurso> sinDisponibilidad = new ArrayList<>();

        for (CategoriaRecurso categoria : reserva.getCategoriasSolicitadas()) {
            Recurso elegido = null;
            for (Recurso candidato : recursoDAO.listarPorCategoria(categoria.getId())) {
                if (estaLibre(candidato, reserva, activasDelDia, idExcluida)) {
                    elegido = candidato;
                    break;
                }
            }
            if (elegido == null) {
                sinDisponibilidad.add(categoria);
            } else {
                asignados.add(elegido);
            }
        }
        if (!sinDisponibilidad.isEmpty()) {
            throw new SinDisponibilidadException(mensajeDisponibilidad(sinDisponibilidad));
        }
        return asignados;
    }

    private List<Reserva> reservasActivasDelDia(LocalDate fecha) {
        List<Reserva> activas = new ArrayList<>();
        for (Reserva otra : reservaDAO.listarPorFecha(fecha)) {
            if (otra != null && otra.isActiva()) {
                activas.add(otra);
            }
        }
        return activas;
    }

    private boolean estaLibre(Recurso recurso, Reserva reserva, List<Reserva> activasDelDia, int idExcluida) {
        for (Reserva otra : activasDelDia) {
            if (otra.getId() == idExcluida) {
                continue;
            }
            if (tieneRecurso(otra, recurso.getId()) && haySolapamiento(reserva, otra)) {
                return false;
            }
        }
        return true;
    }

    private boolean tieneRecurso(Reserva reserva, String idRecurso) {
        for (Recurso recurso : reserva.getRecursosAsignados()) {
            if (recurso != null && idRecurso != null && idRecurso.equals(recurso.getId())) {
                return true;
            }
        }
        return false;
    }

    private boolean haySolapamiento(Reserva a, Reserva b) {
        if (!a.getFecha().equals(b.getFecha())) {
            return false;
        }
        return a.getHoraInicio().isBefore(b.getHoraFin()) && b.getHoraInicio().isBefore(a.getHoraFin());
    }

    // ------------------------------ validar ------------------------------

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
        reserva.setActividad(reserva.getActividad().trim());
        validarDatosDisponibilidad(reserva);
        validarCategorias(reserva.getCategoriasSolicitadas());
    }

    private void validarDatosDisponibilidad(Reserva reserva) {
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
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de finalización.");
        }
        if (reserva.getCategoriasSolicitadas() == null || reserva.getCategoriasSolicitadas().isEmpty()) {
            throw new IllegalArgumentException("Debe solicitar al menos una categoría.");
        }
    }

    private void validarCategorias(List<CategoriaRecurso> categorias) {
        Set<Integer> vistas = new HashSet<>();
        for (CategoriaRecurso categoria : categorias) {
            if (categoria == null) {
                throw new IllegalArgumentException("La reserva no puede contener categorías nulas.");
            }
            if (!vistas.add(categoria.getId())) {
                throw new IllegalArgumentException(
                        "La categoría '" + nombreCategoria(categoria) + "' fue solicitada más de una vez.");
            }
        }
    }

    private String mensajeDisponibilidad(List<CategoriaRecurso> categorias) {
        return "No hay disponibilidad para: "
                + categorias.stream().map(this::nombreCategoria).collect(Collectors.joining(", ")) + ".";
    }

    private String nombreCategoria(CategoriaRecurso categoria) {
        if (categoria.getNombre() != null && !categoria.getNombre().trim().isEmpty()) {
            return categoria.getNombre();
        }
        if (categoria.getDescripcion() != null && !categoria.getDescripcion().trim().isEmpty()) {
            return categoria.getDescripcion();
        }
        return String.valueOf(categoria.getId());
    }

    private static class SinDisponibilidadException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;

        SinDisponibilidadException(String mensaje) {
            super(mensaje);
        }
    }

    // --------------------------------- IA ---------------------------------

    /**
     * Operación RESERVA_EXTRAER_IA. Clave, URL y modelo salen de variables de entorno del
     * servidor (nunca del repositorio); sin ellas usa el proxy demo del curso. Si falla,
     * el mensaje es legible y el formulario manual sigue funcionando.
     */
    @Override
    public ReservaExtraccion extraerReserva(String frase) {
        if (frase == null || frase.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe describir la reserva en una frase.");
        }

        OpenAiChatModel aiModel = OpenAiChatModel.builder()
                .baseUrl(System.getenv().getOrDefault("LLM_BASE_URL", "http://langchain4j.dev/demo/openai/v1"))
                .apiKey(System.getenv().getOrDefault("LLM_API_KEY", "demo"))
                .modelName(System.getenv().getOrDefault("LLM_MODEL", "gpt-4o-mini"))
                .timeout(Duration.ofSeconds(30))
                .maxRetries(1)
                .build();

        ReservaExtractorService aiService = AiServices.create(ReservaExtractorService.class, aiModel);

        List<String> descripciones = proveedorCategorias.get().stream()
                .map(CategoriaRecurso::getDescripcion)
                .collect(Collectors.toList());

        try {
            return aiService.extraer(frase, formatearCategorias(descripciones), LocalDate.now().toString());
        } catch (RuntimeException e) {
            throw new IllegalStateException("No se pudo interpretar la reserva con IA: " + e.getMessage(), e);
        }
    }

    private String formatearCategorias(List<String> descripciones) {
        if (descripciones == null || descripciones.isEmpty()) {
            return "(no hay categorías registradas)";
        }
        StringBuilder sb = new StringBuilder();
        for (String descripcion : descripciones) {
            sb.append("- ").append(descripcion).append("\n");
        }
        return sb.toString();
    }
}