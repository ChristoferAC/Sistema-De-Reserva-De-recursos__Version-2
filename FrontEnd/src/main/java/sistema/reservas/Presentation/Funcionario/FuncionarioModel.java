package sistema.reservas.Presentation.Funcionario;

import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Presentation.AbstractModel;

import java.util.ArrayList;
import java.util.List;

public class FuncionarioModel extends AbstractModel {

    public static final String CURRENT = "current";
    public static final String FUNCIONARIOS = "funcionarios";

    private Funcionario current;
    private List<Funcionario> funcionarios;

    public FuncionarioModel() {
        current = null;
        funcionarios = new ArrayList<>();
    }

    public Funcionario getCurrent() {
        return current;
    }

    public void setCurrent(Funcionario current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
        firePropertyChange(FUNCIONARIOS);
    }
}
