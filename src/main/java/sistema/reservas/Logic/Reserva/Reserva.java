package sistema.reservas.Logic.Reserva;

import sistema.reservas.Logic.Categoria.CategoriaRecurso;
import sistema.reservas.Logic.Funcionario.Funcionario;
import sistema.reservas.Logic.Recurso.Recurso;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class Reserva {
    private int id;
    private Funcionario funcionario;
    private String actividad;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    private List<CategoriaRecurso> categoriasSolicitadas;
    private List<Recurso> recursosAsignados;

    public Reserva() {
        categoriasSolicitadas = new ArrayList<>();
        recursosAsignados = new ArrayList<>();
    }

    public Reserva(int id, Funcionario funcionario, String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        this.id = id;
        this.funcionario = funcionario;
        this.actividad = actividad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;

        this.categoriasSolicitadas = new ArrayList<>();
        this.recursosAsignados = new ArrayList<>();
    }

    public int getId() { return id;}

    public void setId(int id) { this.id = id;}

    public Funcionario getFuncionario() { return funcionario;}

    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario;}

    public String getActividad() {return actividad;}

    public void setActividad(String actividad) { this.actividad = actividad;}

    public LocalDate getFecha() { return fecha;}

    public void setFecha(LocalDate fecha) { this.fecha = fecha;}

    public LocalTime getHoraInicio() { return horaInicio;}

    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio;}

    public LocalTime getHoraFin() { return horaFin;}

    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin;}

    public List<CategoriaRecurso> getCategoriasSolicitadas() { return categoriasSolicitadas;}

    public List<Recurso> getRecursosAsignados() { return recursosAsignados;}

    public void agregarCategoria(CategoriaRecurso categoria) {categoriasSolicitadas.add(categoria);}

    public void agregarRecurso(Recurso recurso) {recursosAsignados.add(recurso);}

    public void limpiarRecursosAsignados() {recursosAsignados.clear();}
}