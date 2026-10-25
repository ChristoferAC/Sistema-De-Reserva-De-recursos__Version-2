package sistema.reservas.Presentation.Funcionario;

import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class FuncionarioController {

    private final FuncionarioPanel view;
    private final FuncionarioModel model;

    public FuncionarioController(FuncionarioPanel view, FuncionarioModel model) {
        this.view = view;
        this.model = model;

        view.setModel(model);

        this.view.getBtnBuscar().addActionListener(e -> buscar());
        this.view.getBtnGuardar().addActionListener(e -> guardar());
        this.view.getBtnBorrar().addActionListener(e -> borrar());
        this.view.getBtnLimpiar().addActionListener(e -> limpiar());
        this.view.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        cargarTodos();
    }

    private void cargarTodos() {
        try {
            model.setFuncionarios(Service.instance().findAll(new Funcionario()));
        } catch (Exception ex) {
            mostrar(ex.getMessage());
        }
    }

    private void buscar() {
        String idTexto = view.getTxtBuscarId().getText().trim();
        String nombre = view.getTxtBuscarNombre().getText().trim();

        try {
            if (!idTexto.isEmpty()) {
                Funcionario filtro = new Funcionario();
                filtro.setId(Integer.parseInt(idTexto));
                try {
                    model.setFuncionarios(List.of(Service.instance().read(filtro)));
                } catch (Exception noExiste) {
                    model.setFuncionarios(List.of());
                }
            } else if (!nombre.isEmpty()) {
                model.setFuncionarios(Service.instance().findFuncionariosByNombre(nombre));
            } else {
                cargarTodos();
            }
        } catch (NumberFormatException ex) {
            mostrar("El ID debe ser numérico.");
        } catch (Exception ex) {
            mostrar(ex.getMessage());
        }
    }

    private void guardar() {
        try {
            String idTexto = view.getTxtId().getText().trim();
            String nombre = view.getTxtNombre().getText().trim();
            String telefono = view.getTxtTelefono().getText().trim();
            String username = view.getTxtUsername().getText().trim();

            if (idTexto.isEmpty()) {
                // Nuevo funcionario: se pide un ID ya que no es autogenerado
                mostrar("Debe indicar el ID del funcionario.");
                return;
            }

            int id = Integer.parseInt(idTexto);
            // Se arma el objeto con lo del formulario; la clave la maneja el Backend
            Funcionario funcionario = new Funcionario(id, nombre, username, null, telefono);

            Funcionario actual = model.getCurrent();
            if (actual != null && actual.getId() == id) {
                Service.instance().update(funcionario);
            } else {
                Service.instance().create(funcionario);
            }

            model.setCurrent(null);
            cargarTodos();

        } catch (NumberFormatException ex) {
            mostrar("El ID debe ser numérico.");
        } catch (Exception ex) {
            mostrar(ex.getMessage());
        }
    }

    private void borrar() {
        String idTexto = view.getTxtId().getText().trim();
        if (idTexto.isEmpty()) {
            mostrar("Seleccione un funcionario para borrar.");
            return;
        }
        try {
            Funcionario funcionario = new Funcionario();
            funcionario.setId(Integer.parseInt(idTexto));
            Service.instance().delete(funcionario);
            model.setCurrent(null);
            cargarTodos();
        } catch (NumberFormatException ex) {
            mostrar("El ID debe ser numérico.");
        } catch (Exception ex) {
            mostrar(ex.getMessage());
        }
    }

    private void limpiar() {
        model.setCurrent(null);
    }

    private void cargarSeleccion() {
        int fila = view.getTabla().getSelectedRow();
        if (fila < 0) return;

        DefaultTableModel tableModel = view.getTableModel();
        Funcionario filtro = new Funcionario();
        filtro.setId(Integer.parseInt(tableModel.getValueAt(fila, 0).toString()));

        try {
            model.setCurrent(Service.instance().read(filtro));
        } catch (Exception ex) {
            mostrar(ex.getMessage());
        }
    }

    private void mostrar(String mensaje) {
        JOptionPane.showMessageDialog(view.getPanel1(), mensaje);
    }
}