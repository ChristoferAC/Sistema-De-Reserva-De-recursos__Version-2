package sistema.reservas.Presentation.Funcionario;

import sistema.reservas.Logic.Funcionario;
import sistema.reservas.dao.FuncionarioDAO;

import java.util.List;

public class FuncionarioService {

    private final FuncionarioDAO funcionarioDAO;

    public FuncionarioService(FuncionarioDAO funcionarioDAO) {
        this.funcionarioDAO = funcionarioDAO;
    }

    public Funcionario buscarPorId(int id) {
        return funcionarioDAO.buscarPorId(id);
    }

    public List<Funcionario> buscarPorNombre(String nombre) {
        return funcionarioDAO.buscarPorNombre(nombre);
    }

    public List<Funcionario> listarTodos() {
        return funcionarioDAO.listarTodos();
    }

    public void crear(Funcionario funcionario) {
        validar(funcionario);
        if (funcionarioDAO.buscarPorId(funcionario.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un funcionario con ese ID.");
        }
        // Regla del enunciado: la clave inicial del usuario queda igual al id
        funcionario.setPassword(String.valueOf(funcionario.getId()));
        funcionarioDAO.guardar(funcionario);
    }

    public void actualizar(Funcionario funcionario) {
        validar(funcionario);
        if (funcionarioDAO.buscarPorId(funcionario.getId()) == null) {
            throw new IllegalArgumentException("No existe un funcionario con ese ID.");
        }
        funcionarioDAO.actualizar(funcionario);
    }

    public void eliminar(int id) {
        if (funcionarioDAO.buscarPorId(id) == null) {
            throw new IllegalArgumentException("No existe un funcionario con ese ID.");
        }
        funcionarioDAO.eliminar(id);
    }

    private void validar(Funcionario funcionario) {
        if (funcionario.getNombre() == null || funcionario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (funcionario.getTelefono() == null || funcionario.getTelefono().isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }
        if (funcionario.getUsername() == null || funcionario.getUsername().isBlank()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
    }
}