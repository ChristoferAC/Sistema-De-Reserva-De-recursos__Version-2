package sistema.reservas.unit;

import sistema.reservas.dao.FuncionarioDAO;
import sistema.reservas.model.Funcionario;

import java.util.ArrayList;
import java.util.List;

/** DAO falso en memoria, solo para pruebas unitarias de FuncionarioService. */
public class FuncionarioDAOFalso implements FuncionarioDAO {

    private final List<Funcionario> funcionarios = new ArrayList<>();

    @Override
    public Funcionario buscarPorId(int id) {
        for (Funcionario f : funcionarios) {
            if (f.getId() == id) {
                return f;
            }
        }
        return null;
    }

    @Override
    public List<Funcionario> buscarPorNombre(String nombre) {
        List<Funcionario> resultado = new ArrayList<>();
        for (Funcionario f : funcionarios) {
            if (f.getNombre() != null && f.getNombre().toLowerCase().contains(nombre.toLowerCase())) {
                resultado.add(f);
            }
        }
        return resultado;
    }

    @Override
    public void guardar(Funcionario funcionario) {
        funcionarios.add(funcionario);
    }

    @Override
    public void actualizar(Funcionario funcionario) {
        for (int i = 0; i < funcionarios.size(); i++) {
            if (funcionarios.get(i).getId() == funcionario.getId()) {
                funcionarios.set(i, funcionario);
                return;
            }
        }
    }

    @Override
    public void eliminar(int id) {
        funcionarios.removeIf(f -> f.getId() == id);
    }

    @Override
    public List<Funcionario> listarTodos() {
        return new ArrayList<>(funcionarios);
    }
}