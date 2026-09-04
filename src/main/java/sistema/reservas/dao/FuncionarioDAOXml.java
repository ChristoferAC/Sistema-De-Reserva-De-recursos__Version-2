package sistema.reservas.dao;

import sistema.reservas.Logic.Funcionario.Funcionario;
import sistema.reservas.Logic.Usuario.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación real de FuncionarioDAO usando XML.
 *
 * Reutiliza UsuarioDAOXml por dentro: ambos DAO leen/escriben el MISMO
 * archivo data/usuarios.xml (ver /docs/CONTRATO_XML.md). Así se evita que
 * dos DAO distintos dupliquen la lógica de lectura/escritura de XML y
 * corran el riesgo de desincronizarse.
 */
public class FuncionarioDAOXml implements FuncionarioDAO {

    private final UsuarioDAOXml usuarioDAO = new UsuarioDAOXml();

    @Override
    public Funcionario buscarPorId(int id) {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        return (usuario instanceof Funcionario) ? (Funcionario) usuario : null;
    }

    @Override
    public List<Funcionario> buscarPorNombre(String nombre) {
        List<Funcionario> resultado = new ArrayList<>();
        for (Funcionario f : listarTodos()) {
            if (f.getNombre() != null && f.getNombre().toLowerCase().contains(nombre.toLowerCase())) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    @Override
    public void guardar(Funcionario funcionario) {
        usuarioDAO.guardar(funcionario);
    }

    @Override
    public void actualizar(Funcionario funcionario) {
        usuarioDAO.actualizar(funcionario);
    }

    @Override
    public void eliminar(int id) {
        usuarioDAO.eliminar(id);
    }

    @Override
    public List<Funcionario> listarTodos() {
        List<Funcionario> funcionarios = new ArrayList<>();
        for (Usuario u : usuarioDAO.listarTodos()) {
            if (u instanceof Funcionario) {
                funcionarios.add((Funcionario) u);
            }
        }
        return funcionarios;
    }
}