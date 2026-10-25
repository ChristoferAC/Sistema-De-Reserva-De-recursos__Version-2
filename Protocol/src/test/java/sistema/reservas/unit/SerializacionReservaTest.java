package sistema.reservas.unit;

import org.junit.jupiter.api.Test;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;
import sistema.reservas.Logic.ReservaExtraccion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SerializacionReservaTest {

    /** Simula lo que hace el socket: escribe el objeto y lo vuelve a leer. */
    @SuppressWarnings("unchecked")
    private static <T> T viajar(T objeto) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(objeto);
        } catch (NotSerializableException e) {
            fail("No es Serializable: " + e.getMessage()
                    + ". Si es Usuario/Funcionario/CategoriaRecurso avisa a I1/I3; si es tuya, corrígela.");
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) in.readObject();
        }
    }

    @Test
    void reservaCompleta_sobreviveAlViaje() throws Exception {
        CategoriaRecurso categoria = new CategoriaRecurso(1, "", "Elementos Electronicos");
        Funcionario funcionario = new Funcionario(1234, "Juan Perez", "1234", null, "88880000");
        Recurso recurso = new Recurso("1111", "Laptop", "Laptop Dell", categoria);

        Reserva reserva = new Reserva(7, funcionario, "Reunión",
                LocalDate.of(2026, 9, 10), LocalTime.of(10, 0), LocalTime.of(11, 0));
        reserva.agregarCategoria(categoria);
        reserva.agregarRecurso(recurso);

        Reserva copia = viajar(reserva);

        assertNotSame(reserva, copia);
        assertEquals(7, copia.getId());
        assertEquals("Reunión", copia.getActividad());
        assertEquals(LocalDate.of(2026, 9, 10), copia.getFecha());
        assertEquals(LocalTime.of(10, 0), copia.getHoraInicio());
        assertEquals(LocalTime.of(11, 0), copia.getHoraFin());
        assertEquals(Reserva.Estado.ACTIVA, copia.getEstado());
        assertEquals(1234, copia.getFuncionario().getId());
        assertEquals("Elementos Electronicos", copia.getCategoriasSolicitadas().get(0).getDescripcion());
        assertEquals("1111", copia.getRecursosAsignados().get(0).getId());
    }

    @Test
    void recurso_sobreviveAlViaje() throws Exception {
        Recurso recurso = new Recurso("2223", "Salon Pequeño", "Salon para Eventos",
                new CategoriaRecurso(2, "", "Salones"));

        Recurso copia = viajar(recurso);

        assertEquals("2223", copia.getId());
        assertEquals("Salon Pequeño", copia.getNombre());
        assertEquals(2, copia.getCategoria().getId());
    }

    @Test
    void extraccionDeIA_sobreviveAlViaje() throws Exception {
        ReservaExtraccion extraccion = new ReservaExtraccion("Reunión", "2026-09-10", "10:00", "11:00",
                List.of("Elementos Electronicos"));

        ReservaExtraccion copia = viajar(extraccion);

        assertEquals("Reunión", copia.getActividad());
        assertEquals("11:00", copia.getHoraFinal());
        assertEquals(List.of("Elementos Electronicos"), copia.getCategoriasRecurso());
    }
}