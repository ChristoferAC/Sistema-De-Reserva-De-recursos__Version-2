package sistema.reservas.Logic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Funcionario extends Usuario {

    private String telefono;

    /** JAXB necesita un constructor vacio para poder des-serializar. */
    public Funcionario() {
        super();
    }

    public Funcionario(int id, String nombre, String username, String password, String telefono) {
        super(id, nombre, username, password);
        this.telefono = telefono;
    }

    @Override
    public String getRol() {
        return "FUNCIONARIO";
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}