package sistema.reservas.Logic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Administrador extends Usuario {

    /** JAXB necesita un constructor vacio para poder des-serializar. */
    public Administrador() {
        super();
    }

    public Administrador(int id, String nombre, String username, String password) {
        super(id, nombre, username, password);
    }

    @Override
    public String getRol() {
        return "ADMIN";
    }
}