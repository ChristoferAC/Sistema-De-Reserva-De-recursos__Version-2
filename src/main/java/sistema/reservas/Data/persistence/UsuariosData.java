package sistema.reservas.Data.persistence;

import sistema.reservas.Logic.Usuario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Envoltorio raiz para serializar/des-serializar la lista completa de
 * Usuarios (Administradores y Funcionarios juntos) a un solo XML.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UsuariosData {

    @XmlElementWrapper(name = "usuarios")
    @XmlElement(name = "usuario")
    private List<Usuario> usuarios = new ArrayList<>();

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
