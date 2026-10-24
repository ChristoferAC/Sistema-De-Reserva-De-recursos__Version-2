package sistema.reservas.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sistema.reservas.Data.persistence.RecursoDAO;
import sistema.reservas.Data.persistence.ReservaDAO;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Logic.Services.Notificador;
import sistema.reservas.Logic.Services.ServiceReserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** Pruebas unitarias de ServiceReserva con DAO falso (Surefire): no tocan MySQL. */
class ServiceReservaTest {

    private static final LocalDate DIA = LocalDate.now().plusDays(10);

    private FakeRecursoDAO recursoDAO;
    private FakeReservaDAO reservaDAO;
    private ServiceReserva service;
    private Funcionario funcionario;
    private CategoriaRecurso laptops;   // id 1 -> recursos L1, L2
    private CategoriaRecurso salones;   // id 2 -> recurso S1 (uno solo)

    @BeforeEach
    void setUp() {
        funcionario = new Funcionario(111, "Juan Perez", "111", null, "88880000");
        laptops = new CategoriaRecurso(1, "", "Laptops");
        salones = new CategoriaRecurso(2, "", "Salones");

        recursoDAO = new FakeRecursoDAO();
        recursoDAO.agregar(new Recurso("L1", "Laptop 1", "Laptop uno", laptops));
        recursoDAO.agregar(new Recurso("L2", "Laptop 2", "Laptop dos", laptops));
        recursoDAO.agregar(new Recurso("S1", "Salon 1", "Salon uno", salones));

        reservaDAO = new FakeReservaDAO();
        service = new ServiceReserva(reservaDAO, recursoDAO);
    }

    // ------------------------------ ayudas ------------------------------

    private Reserva reserva(LocalDate fecha, int horaInicio, int horaFin, CategoriaRecurso... categorias) {
        Reserva r = new Reserva(0, funcionario, "Reunión", fecha,
                LocalTime.of(horaInicio, 0), LocalTime.of(horaFin, 0));
        for (CategoriaRecurso categoria : categorias) {
            r.agregarCategoria(categoria);
        }
        return r;
    }

    /** Ids de los recursos asignados, p. ej. "L1,S1". */
    private String recursos(Reserva r) {
        List<String> ids = new ArrayList<>();
        for (Recurso recurso : r.getRecursosAsignados()) {
            ids.add(recurso.getId());
        }
        return String.join(",", ids);
    }

    // -------------------------------- crear --------------------------------

    @Test
    void crear_devuelveReservaConIdGeneradoYPrimerRecursoLibre() {
        Reserva creada = service.crearReserva(reserva(DIA, 9, 11, laptops));

        assertTrue(creada.getId() > 0);
        assertEquals(Reserva.Estado.ACTIVA, creada.getEstado());
        assertEquals("L1", recursos(creada));
        assertEquals(1, reservaDAO.listar().size());
    }

    @Test
    void crear_asignaUnRecursoPorCadaCategoria() {
        Reserva creada = service.crearReserva(reserva(DIA, 9, 11, laptops, salones));

        assertEquals("L1,S1", recursos(creada));
    }

    @Test
    void crear_ignoraElIdQueTraeElCliente() {
        Reserva enviada = reserva(DIA, 9, 11, laptops);
        enviada.setId(999);

        Reserva creada = service.crearReserva(enviada);

        assertNotEquals(999, creada.getId());   // el id lo asigna la BD (H4)
    }

    // ------------------- solapamiento y disponibilidad -------------------

    @Test
    void solapamientoExacto_usaElSiguienteRecurso_yLuegoNoHayDisponibilidad() {
        service.crearReserva(reserva(DIA, 9, 11, laptops));                    // L1
        Reserva segunda = service.crearReserva(reserva(DIA, 9, 11, laptops));  // misma hora -> L2
        assertEquals("L2", recursos(segunda));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearReserva(reserva(DIA, 9, 11, laptops)));

        assertEquals("No hay disponibilidad para: Laptops.", ex.getMessage());
        assertEquals(2, reservaDAO.listar().size());   // la rechazada no dejó nada
    }

    @Test
    void solapamientoParcial_noPermiteElMismoRecurso() {
        service.crearReserva(reserva(DIA, 9, 11, laptops));                      // L1
        Reserva parcial = service.crearReserva(reserva(DIA, 10, 12, laptops));   // se traslapa -> L2

        assertEquals("L2", recursos(parcial));
    }

    @Test
    void contiguo_terminaCuandoOtraEmpieza_siSePermite() {
        service.crearReserva(reserva(DIA, 9, 10, laptops));                      // L1
        Reserva contigua = service.crearReserva(reserva(DIA, 10, 11, laptops));  // empieza cuando la otra termina

        assertEquals("L1", recursos(contigua));
    }

    @Test
    void otraFecha_noCuentaComoSolapamiento() {
        service.crearReserva(reserva(DIA, 9, 11, laptops));
        Reserva otroDia = service.crearReserva(reserva(DIA.plusDays(1), 9, 11, laptops));

        assertEquals("L1", recursos(otroDia));
    }

    @Test
    void sinDisponibilidad_listaTodasLasCategoriasQueFaltan() {
        service.crearReserva(reserva(DIA, 9, 11, laptops, salones));   // L1 y S1
        service.crearReserva(reserva(DIA, 9, 11, laptops));            // L2

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearReserva(reserva(DIA, 9, 11, laptops, salones)));

        assertEquals("No hay disponibilidad para: Laptops, Salones.", ex.getMessage());
    }

    @Test
    void hayDisponibilidad_reflejaElEstadoActual() {
        assertTrue(service.hayDisponibilidad(reserva(DIA, 9, 11, salones)));

        service.crearReserva(reserva(DIA, 9, 11, salones));

        assertFalse(service.hayDisponibilidad(reserva(DIA, 10, 12, salones)));
        assertTrue(service.hayDisponibilidad(reserva(DIA, 11, 12, salones)));   // contigua: sí
    }

    // ------------------------------ cancelar ------------------------------

    @Test
    void cancelar_marcaCanceladaLiberaRecursosYNoBorra() {
        Reserva creada = service.crearReserva(reserva(DIA, 9, 11, laptops));

        service.cancelarReserva(creada.getId());

        Reserva leida = service.buscarReserva(creada.getId());
        assertNotNull(leida);   // sigue en el historial
        assertEquals(Reserva.Estado.CANCELADA, leida.getEstado());
        assertTrue(leida.getRecursosAsignados().isEmpty());
        assertEquals(1, leida.getCategoriasSolicitadas().size());
    }

    @Test
    void cancelar_liberaElRecursoParaOtraReserva() {
        Reserva primera = service.crearReserva(reserva(DIA, 9, 11, salones));
        assertThrows(IllegalArgumentException.class,
                () -> service.crearReserva(reserva(DIA, 9, 11, salones)));   // el único salón está ocupado

        service.cancelarReserva(primera.getId());

        Reserva segunda = service.crearReserva(reserva(DIA, 9, 11, salones));
        assertEquals("S1", recursos(segunda));
    }

    @Test
    void cancelar_rechazaInexistenteYaCanceladaYPasada() {
        assertThrows(IllegalArgumentException.class, () -> service.cancelarReserva(999));

        Reserva r = service.crearReserva(reserva(DIA, 9, 11, laptops));
        service.cancelarReserva(r.getId());
        IllegalArgumentException yaCancelada = assertThrows(IllegalArgumentException.class,
                () -> service.cancelarReserva(r.getId()));
        assertEquals("La reserva ya se encuentra cancelada.", yaCancelada.getMessage());

        // crearReserva no bloquea fechas pasadas; solo cancelar exige que sea futura
        Reserva pasada = service.crearReserva(reserva(LocalDate.now().minusDays(1), 9, 11, laptops));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.cancelarReserva(pasada.getId()));
        assertEquals("Solo se pueden cancelar reservas futuras.", ex.getMessage());
    }

    // ------------------------------ modificar ------------------------------

    @Test
    void modificar_noSeBloqueaASiMisma() {
        Reserva creada = service.crearReserva(reserva(DIA, 9, 11, laptops));   // L1
        Reserva cambio = reserva(DIA, 10, 12, laptops);                        // se traslapa con ella misma
        cambio.setId(creada.getId());

        Reserva modificada = service.modificarReserva(cambio);

        assertEquals("L1", recursos(modificada));   // idExcluida: no se vio solapada consigo misma
        assertEquals(LocalTime.of(10, 0), modificada.getHoraInicio());
    }

    @Test
    void modificar_reemplazaCategoriasYRecursosJuntos() {
        Reserva creada = service.crearReserva(reserva(DIA, 9, 11, laptops));
        Reserva cambio = reserva(DIA, 9, 11, salones);
        cambio.setId(creada.getId());

        Reserva modificada = service.modificarReserva(cambio);

        assertEquals("S1", recursos(modificada));
        assertEquals(1, modificada.getCategoriasSolicitadas().size());
        assertEquals(2, modificada.getCategoriasSolicitadas().get(0).getId());
    }

    @Test
    void modificar_rechazaCanceladaEInexistente() {
        Reserva creada = service.crearReserva(reserva(DIA, 9, 11, laptops));
        service.cancelarReserva(creada.getId());
        Reserva cambio = reserva(DIA, 9, 11, laptops);
        cambio.setId(creada.getId());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.modificarReserva(cambio));
        assertEquals("No se puede modificar una reserva cancelada.", ex.getMessage());

        Reserva fantasma = reserva(DIA, 9, 11, laptops);
        fantasma.setId(777);
        assertThrows(IllegalArgumentException.class, () -> service.modificarReserva(fantasma));
    }

    @Test
    void modificar_sinDisponibilidadNoDejaLaReservaAMedias() {
        service.crearReserva(reserva(DIA, 9, 11, laptops));                          // L1
        service.crearReserva(reserva(DIA, 9, 11, laptops));                          // L2
        Reserva tercera = service.crearReserva(reserva(DIA, 12, 13, laptops));       // L1 (libre a esa hora)
        Reserva cambio = reserva(DIA, 9, 11, laptops);                               // chocaría con L1 y L2
        cambio.setId(tercera.getId());

        assertThrows(IllegalArgumentException.class, () -> service.modificarReserva(cambio));

        Reserva intacta = service.buscarReserva(tercera.getId());
        assertEquals(LocalTime.of(12, 0), intacta.getHoraInicio());
        assertEquals("L1", recursos(intacta));
    }

    // ------------------------------ validación ------------------------------

    @Test
    void validaciones_rechazanDatosInvalidosSinGuardarNada() {
        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(null));

        Reserva sinFuncionario = reserva(DIA, 9, 11, laptops);
        sinFuncionario.setFuncionario(null);
        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(sinFuncionario));

        Reserva sinActividad = reserva(DIA, 9, 11, laptops);
        sinActividad.setActividad("   ");
        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(sinActividad));

        Reserva finIgualInicio = reserva(DIA, 9, 9, laptops);
        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(finIgualInicio));

        Reserva finAntesDeInicio = reserva(DIA, 11, 9, laptops);
        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(finAntesDeInicio));

        Reserva sinCategorias = reserva(DIA, 9, 11);
        assertThrows(IllegalArgumentException.class, () -> service.crearReserva(sinCategorias));

        Reserva duplicada = reserva(DIA, 9, 11, laptops, laptops);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.crearReserva(duplicada));
        assertEquals("La categoría 'Laptops' fue solicitada más de una vez.", ex.getMessage());

        assertEquals(0, reservaDAO.listar().size());
    }

    @Test
    void listarReservasFuncionario_filtraPorFuncionario() {
        service.crearReserva(reserva(DIA, 9, 11, laptops));
        Reserva deOtro = reserva(DIA, 14, 15, salones);
        deOtro.setFuncionario(new Funcionario(222, "Maria", "222", null, "99990000"));
        service.crearReserva(deOtro);

        assertEquals(1, service.listarReservasFuncionario(111).size());
        assertEquals(1, service.listarReservasFuncionario(222).size());
        assertEquals(0, service.listarReservasFuncionario(333).size());
        assertThrows(IllegalArgumentException.class, () -> service.listarReservasFuncionario(0));
    }

    @Test
    void extraerReserva_conFraseVacia_falla_sinLlamarAlLlm() {
        assertThrows(IllegalArgumentException.class, () -> service.extraerReserva("   "));
        assertThrows(IllegalArgumentException.class, () -> service.extraerReserva(null));
    }

    // ------------------------ gancho de notificación ------------------------

    @Test
    void notificador_seAvisaUnaVezConLaReservaYaConIdYElOrigen() {
        List<Reserva> avisadas = new ArrayList<>();
        List<Object> origenes = new ArrayList<>();
        Notificador notificador = (reserva, origen) -> {
            avisadas.add(reserva);
            origenes.add(origen);
        };
        Object worker = new Object();
        ServiceReserva conAviso = new ServiceReserva(reservaDAO, recursoDAO, null, notificador, worker);

        Reserva creada = conAviso.crearReserva(reserva(DIA, 9, 11, laptops));

        assertEquals(1, avisadas.size());
        assertEquals(creada.getId(), avisadas.get(0).getId());
        assertTrue(avisadas.get(0).getId() > 0);
        assertEquals("L1", recursos(avisadas.get(0)));
        assertSame(worker, origenes.get(0));
    }

    @Test
    void notificador_noSeLlamaSiLaReservaFalla() {
        int[] llamadas = {0};
        ServiceReserva conAviso = new ServiceReserva(reservaDAO, recursoDAO, null,
                (reserva, origen) -> llamadas[0]++, null);

        conAviso.crearReserva(reserva(DIA, 9, 11, salones));
        assertThrows(IllegalArgumentException.class,
                () -> conAviso.crearReserva(reserva(DIA, 9, 11, salones)));

        assertEquals(1, llamadas[0]);   // solo la que sí se creó
    }

    @Test
    void notificador_siFallaLaReservaIgualQuedaCreada() {
        Notificador roto = (reserva, origen) -> {
            throw new IllegalStateException("cliente caído");
        };
        ServiceReserva conAviso = new ServiceReserva(reservaDAO, recursoDAO, null, roto, null);

        Reserva creada = conAviso.crearReserva(reserva(DIA, 9, 11, laptops));

        assertTrue(creada.getId() > 0);
        assertNotNull(reservaDAO.buscarPorId(creada.getId()));
    }

    // ---------------------------- concurrencia (H5) ----------------------------

    /**
     * Dos Workers, cada uno con SU PROPIO ServiceReserva, piden el único salón a la
     * misma hora. La demora simulada del DAO hace visible la carrera: sin el candado
     * único, las dos pasarían el chequeo antes de que cualquiera inserte.
     */
    @Test
    void concurrencia_dosWorkersMismoRecurso_soloUnoGana() throws Exception {
        reservaDAO.latenciaMs = 40;
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Boolean>> resultados = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            resultados.add(pool.submit(() -> {
                ServiceReserva delWorker = new ServiceReserva(reservaDAO, recursoDAO);   // un Service por Worker
                salida.await();                                                          // arrancan a la vez
                try {
                    delWorker.crearReserva(reserva(DIA, 9, 11, salones));
                    return true;
                } catch (IllegalArgumentException sinDisponibilidad) {
                    return false;
                }
            }));
        }
        salida.countDown();

        int exitos = 0;
        for (Future<Boolean> resultado : resultados) {
            if (resultado.get(10, TimeUnit.SECONDS)) {
                exitos++;
            }
        }
        pool.shutdown();

        assertEquals(1, exitos);
        assertEquals(1, reservaDAO.listar().size());
    }

    // ------------------------------- DAO falsos -------------------------------

    static class FakeRecursoDAO implements RecursoDAO {
        private final List<Recurso> recursos = new ArrayList<>();

        void agregar(Recurso recurso) {
            recursos.add(recurso);
        }

        @Override
        public void insertar(Recurso recurso) {
            recursos.add(recurso);
        }

        @Override
        public Recurso buscarPorId(String id) {
            for (Recurso recurso : recursos) {
                if (recurso.getId().equals(id)) {
                    return recurso;
                }
            }
            return null;
        }

        @Override
        public List<Recurso> listar() {
            return new ArrayList<>(recursos);
        }

        @Override
        public List<Recurso> listarPorCategoria(int idCategoria) {
            List<Recurso> resultado = new ArrayList<>();
            for (Recurso recurso : recursos) {
                if (recurso.getCategoria().getId() == idCategoria) {
                    resultado.add(recurso);
                }
            }
            return resultado;
        }

        @Override
        public void actualizar(Recurso recurso) {
            throw new UnsupportedOperationException("No lo usa ServiceReserva");
        }

        @Override
        public void eliminar(String id) {
            throw new UnsupportedOperationException("No lo usa ServiceReserva");
        }
    }

    /** Guarda y devuelve COPIAS, como una BD real: modificar un objeto leído no cambia lo guardado. */
    static class FakeReservaDAO implements ReservaDAO {
        private final Map<Integer, Reserva> datos = new LinkedHashMap<>();
        private int siguienteId = 1;
        volatile int latenciaMs = 0;   // simula la demora de la BD en las lecturas por fecha

        @Override
        public synchronized Reserva insertar(Reserva reserva) {
            reserva.setId(siguienteId++);
            datos.put(reserva.getId(), copiar(reserva));
            return reserva;
        }

        @Override
        public synchronized Reserva buscarPorId(int id) {
            Reserva guardada = datos.get(id);
            return guardada == null ? null : copiar(guardada);
        }

        @Override
        public synchronized List<Reserva> listar() {
            List<Reserva> copias = new ArrayList<>();
            for (Reserva guardada : datos.values()) {
                copias.add(copiar(guardada));
            }
            return copias;
        }

        @Override
        public synchronized List<Reserva> listarPorFuncionario(int idFuncionario) {
            List<Reserva> copias = new ArrayList<>();
            for (Reserva guardada : datos.values()) {
                if (guardada.getFuncionario().getId() == idFuncionario) {
                    copias.add(copiar(guardada));
                }
            }
            return copias;
        }

        @Override
        public List<Reserva> listarPorFecha(LocalDate fecha) {
            List<Reserva> copias = new ArrayList<>();
            synchronized (this) {
                for (Reserva guardada : datos.values()) {
                    if (guardada.getFecha().equals(fecha)) {
                        copias.add(copiar(guardada));
                    }
                }
            }
            pausa();   // la lectura ya quedó "vieja" mientras otro Worker puede insertar
            return copias;
        }

        @Override
        public synchronized void actualizar(Reserva reserva) {
            if (!datos.containsKey(reserva.getId())) {
                throw new IllegalArgumentException("La reserva no existe.");
            }
            datos.put(reserva.getId(), copiar(reserva));
        }

        private void pausa() {
            if (latenciaMs > 0) {
                try {
                    Thread.sleep(latenciaMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private static Reserva copiar(Reserva original) {
            Reserva copia = new Reserva(original.getId(), original.getFuncionario(), original.getActividad(),
                    original.getFecha(), original.getHoraInicio(), original.getHoraFin());
            copia.setEstado(original.getEstado());
            for (CategoriaRecurso categoria : original.getCategoriasSolicitadas()) {
                copia.agregarCategoria(categoria);
            }
            for (Recurso recurso : original.getRecursosAsignados()) {
                copia.agregarRecurso(recurso);
            }
            return copia;
        }
    }
}