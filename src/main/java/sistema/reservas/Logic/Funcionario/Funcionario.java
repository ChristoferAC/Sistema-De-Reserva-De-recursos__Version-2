package sistema.reservas.Logic.Funcionario;

import sistema.reservas.Logic.Usuario.Usuario;

public class Funcionario extends Usuario {

    private String telefono;

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