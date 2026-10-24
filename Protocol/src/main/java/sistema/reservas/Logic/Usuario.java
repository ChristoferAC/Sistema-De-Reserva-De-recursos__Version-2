package sistema.reservas.Logic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @XmlSeeAlso le dice a JAXB cuales son las subclases concretas que
 * puede encontrarse al serializar/des-serializar una lista de Usuario
 * (ya que Usuario es abstracta, JAXB necesita saber esto de antemano).
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Administrador.class, Funcionario.class})
public abstract class Usuario {
    private int id;
    private String nombre;
    private String username;
    private String password;

    /** JAXB necesita un constructor vacio para poder des-serializar. */
    protected Usuario() {
    }

    public Usuario(int id, String nombre, String username, String password) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.password = password;
    }

    public abstract String getRol();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}