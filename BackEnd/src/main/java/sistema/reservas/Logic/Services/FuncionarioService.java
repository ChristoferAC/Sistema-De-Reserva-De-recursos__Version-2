// ============================================================
// BACKEND / src/main/java/sistema/reservas/Logic/Services/FuncionarioService.java
// Reemplaza al FuncionarioService XML (FaltaPorMover_O_Borrar). Ya no depende de UsuarioService:
// usa el UsuarioDAO directamente.
//
// Conexión con el resto (en el Service del Backend, con la conexión cx del Worker):
//   UsuarioDAO usuarioDao = new UsuarioDAOJdbc(cx);
//   UsuarioService usuarios = new UsuarioService(usuarioDao);
//   FuncionarioService funcionarios = new FuncionarioService(usuarioDao);
// ============================================================
package sistema.reservas.Logic.Services;

import sistema.reservas.Data.persistence.UsuarioDAO;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Funcionarios sobre MySQL. Un Funcionario es un Usuario con rol FUNCIONARIO, así que este
 * Service usa el mismo UsuarioDAO (tabla usuario) y filtra por tipo. Una instancia por Worker.
 * Regla 5: todo Funcionario que sale de aquí lleva password = null.
 */
public class FuncionarioService {

    // Límites de la tabla usuario (sql/01_usuarios_categorias.sql)
    private static final int MAX_NOMBRE = 50;
    private static final int MAX_USERNAME = 30;
    private static final int MAX_TELEFONO = 20;

    private final UsuarioDAO usuarioDAO;

    public FuncionarioService(UsuarioDAO usuarioDAO) {
        if (usuarioDAO == null) {
            throw new IllegalArgumentException("El UsuarioDAO no puede ser nulo.");
        }
        this.usuarioDAO = usuarioDAO;
    }

    /** null si no existe o si ese id es de un administrador. */
    public Funcionario buscarPorId(int id) {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        return usuario instanceof Funcionario ? (Funcionario) usuario : null;
    }

    public List<Funcionario> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return listarTodos();
        }
        String buscado = nombre.trim().toLowerCase();
        List<Funcionario> resultado = new ArrayList<>();
        for (Funcionario funcionario : listarTodos()) {
            if (funcionario.getNombre() != null && funcionario.getNombre().toLowerCase().contains(buscado)) {
                resultado.add(funcionario);
            }
        }
        return resultado;
    }

    public List<Funcionario> listarTodos() {
        List<Funcionario> resultado = new ArrayList<>();
        for (Usuario usuario : usuarioDAO.listar()) {
            if (usuario instanceof Funcionario) {
                resultado.add((Funcionario) usuario);
            }
        }
        return resultado;
    }

    /** Regla del enunciado: la clave inicial queda igual al id. */
    public Funcionario crear(Funcionario funcionario) {
        validar(funcionario);
        if (funcionario.getId() <= 0) {
            throw new IllegalArgumentException("El ID del funcionario debe ser un número positivo.");
        }
        if (usuarioDAO.buscarPorId(funcionario.getId()) != null) {
            throw new IllegalArgumentException("Ya existe un usuario con ese ID.");
        }
        usuarioDAO.insertar(funcionario, String.valueOf(funcionario.getId()));
        funcionario.setPassword(null);   // la clave nunca vuelve al cliente
        return funcionario;
    }

    /** No cambia la clave ni el rol: solo nombre, usuario y teléfono. */
    public void actualizar(Funcionario funcionario) {
        validar(funcionario);
        if (buscarPorId(funcionario.getId()) == null) {
            throw new IllegalArgumentException("No existe un funcionario con ese ID.");
        }
        usuarioDAO.actualizar(funcionario);
    }

    /** Si tiene reservas, el DAO responde "No se puede borrar: tiene reservas." */
    public void eliminar(int id) {
        if (buscarPorId(id) == null) {
            throw new IllegalArgumentException("No existe un funcionario con ese ID.");
        }
        usuarioDAO.eliminar(id);
    }

    private void validar(Funcionario funcionario) {
        if (funcionario == null) {
            throw new IllegalArgumentException("El funcionario no puede ser nulo.");
        }
        if (funcionario.getNombre() == null || funcionario.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (funcionario.getTelefono() == null || funcionario.getTelefono().isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio.");
        }
        if (funcionario.getUsername() == null || funcionario.getUsername().isBlank()) {
            throw new IllegalArgumentException("El usuario es obligatorio.");
        }
        funcionario.setNombre(funcionario.getNombre().trim());
        funcionario.setTelefono(funcionario.getTelefono().trim());
        funcionario.setUsername(funcionario.getUsername().trim());

        if (funcionario.getNombre().length() > MAX_NOMBRE) {
            throw new IllegalArgumentException("El nombre no puede superar los " + MAX_NOMBRE + " caracteres.");
        }
        if (funcionario.getUsername().length() > MAX_USERNAME) {
            throw new IllegalArgumentException("El usuario no puede superar los " + MAX_USERNAME + " caracteres.");
        }
        if (funcionario.getTelefono().length() > MAX_TELEFONO) {
            throw new IllegalArgumentException("El teléfono no puede superar los " + MAX_TELEFONO + " caracteres.");
        }
    }
}