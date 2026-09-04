package sistema.reservas.dao;

import sistema.reservas.Logic.Funcionario.Funcionario;
import java.util.List;

public interface FuncionarioDAO {
    Funcionario buscarPorId(int id);
    List<Funcionario> buscarPorNombre(String nombre);
    void guardar(Funcionario funcionario);
    void actualizar(Funcionario funcionario);
    void eliminar(int id);
    List<Funcionario> listarTodos();
}