package sistema.reservas.Logic.Administrador;

import sistema.reservas.Logic.Usuario.Usuario;

public class Administrador extends Usuario {

    public Administrador(int id, String nombre, String username, String password) {
        super(id, nombre, username, password);
    }

    @Override
    public String getRol() {
        return "ADMIN";
    }
}