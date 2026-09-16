package sistema.reservas.Logic;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "administrador")
@XmlAccessorType(XmlAccessType.FIELD)
public class Administrador extends Usuario {

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