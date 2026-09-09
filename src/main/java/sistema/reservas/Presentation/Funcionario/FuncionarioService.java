package sistema.reservas.Presentation.Funcionario;

import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Usuario;
import sistema.reservas.Presentation.Login.UsuarioService;

import java.util.ArrayList;
import java.util.List;

/**
 * Los Funcionarios son un tipo de Usuario, asi que viven en la misma
 * lista/archivo que los Administradores (data/usuarios.xml) - este
 * Service simplemente filtra esa lista por los que son Funcionario,
 * y delega en UsuarioService para persistir cualquier cambio.
 */
public class FuncionarioService {

    public Funcionario buscarPorId(int id) {
        for (Usuario u : UsuarioService.listarTodos()) {
            if (u instanceof Funcionario && u.getId() == id) {
                return (Funcionario) u;
            }
        }
        return null;
    }

    public List<Funcionario> buscarPorNombre(String nombre) {
        List<Funcionario> resultado = new ArrayList<>();
        for (Usuario u : UsuarioService.listarTodos()) {
            if (u instanceof Funcionario && u.getNombre().toLowerCase().contains(nombre.toLowerCase())) {
                resultado.add((Funcionario) u);
            }
        }
        return resultado;
    }

    public List<Funcionario> listarTodos() {
        List<Funcionario> resultado = new ArrayList<>();
        for (Usuario u : UsuarioService.listarTodos()) {
            if (u instanceof Funcionario) {
                resultado.add((Funcionario) u);
            }
        }
        return resultado;
    }

    public void crear(Funcionario funcionario) {
        validar(funcionario);
        if (buscarPorId(funcionario.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un funcionario con ese ID.");
        }
        // Regla del enunciado: la clave inicial del usuario queda igual al id.
        funcionario.setPassword(String.valueOf(funcionario.getId()));
        UsuarioService.registrar(funcionario);
    }

    public void actualizar(Funcionario funcionario) {
        validar(funcionario);
        if (buscarPorId(funcionario.getId()) == null) {
            throw new IllegalArgumentException("No existe un funcionario con ese ID.");
        }
        // El Controller ya modifico los campos directamente sobre la
        // misma instancia que esta en la lista; solo falta guardar.
        UsuarioService.guardarCambios();
    }

    public void eliminar(int id) {
        Funcionario existente = buscarPorId(id);
        if (existente == null) {
            throw new IllegalArgumentException("No existe un funcionario con ese ID.");
        }
        UsuarioService.eliminar(existente);
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