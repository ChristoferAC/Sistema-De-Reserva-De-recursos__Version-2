package sistema.reservas.Data.llm;
import java.util.List;
import java.io.Serializable;

public class ReservaExtraccion implements Serializable  {
    private String actividad;
    private String fecha;
    private String horaInicio;
    private String horaFinal;
    private List<String> categoriasRecurso;

    private static final long serialVersionUID = 1L;

    public ReservaExtraccion() {}

    public ReservaExtraccion(String actividad, String fecha, String horaInicio, String horaFinal, List<String> categoriasRecurso) {
        this.actividad = actividad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFinal = horaFinal;
        this.categoriasRecurso = categoriasRecurso;
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
    public void setCategoriasRecurso(List<String> categoriasRecurso) { this.categoriasRecurso = categoriasRecurso;
    }
}
