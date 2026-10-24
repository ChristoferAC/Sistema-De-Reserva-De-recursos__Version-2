// ============================================================
// PROTOCOL / src/main/java/sistema/reservas/Logic/ReservaExtraccion.java
// Reemplaza a Data/llm/ReservaExtraccion.java (borrar el archivo viejo).
// Al moverla, cambiar el import "sistema.reservas.Data.llm.ReservaExtraccion"
// por "sistema.reservas.Logic.ReservaExtraccion" en ServiceReserva y ReservaController.
// ============================================================
package sistema.reservas.Logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReservaExtraccion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String actividad;
    private String fecha;                        // ISO yyyy-MM-dd (o null)
    private String horaInicio;                   // HH:mm (o null)
    private String horaFinal;                    // HH:mm (o null)
    private List<String> categoriasRecurso;      // ArrayList: serializable seguro por el socket

    public ReservaExtraccion() {
    }

    public ReservaExtraccion(String actividad, String fecha, String horaInicio,
                             String horaFinal, List<String> categoriasRecurso) {
        this.actividad = actividad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFinal = horaFinal;
        setCategoriasRecurso(categoriasRecurso);
    }

    public String getActividad() { return actividad; }
    public void setActividad(String actividad) { this.actividad = actividad; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    public String getHoraFinal() { return horaFinal; }
    public void setHoraFinal(String horaFinal) { this.horaFinal = horaFinal; }

    public List<String> getCategoriasRecurso() { return categoriasRecurso; }

    /** Copia a ArrayList: el proxy de LangChain4j puede devolver listas no serializables. */
    public void setCategoriasRecurso(List<String> categoriasRecurso) {
        this.categoriasRecurso = categoriasRecurso == null ? null : new ArrayList<>(categoriasRecurso);
    }
}