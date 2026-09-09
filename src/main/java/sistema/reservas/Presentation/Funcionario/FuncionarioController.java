package sistema.reservas.Presentation.Funcionario;

import sistema.reservas.Logic.Funcionario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FuncionarioController {

    private final FuncionarioPanel view;
    private final FuncionarioModel model;
    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioPanel view, FuncionarioModel model) {
        this.view = view;
        this.model = model;
        this.funcionarioService = new FuncionarioService();

        view.setModel(model);

        this.view.getBtnBuscar().addActionListener(e -> buscar());
        this.view.getBtnGuardar().addActionListener(e -> guardar());
        this.view.getBtnBorrar().addActionListener(e -> borrar());
        this.view.getBtnLimpiar().addActionListener(e -> limpiar());
        this.view.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        model.setFuncionarios(funcionarioService.listarTodos());
    }

    private void buscar() {
        String idTexto = view.getTxtBuscarId().getText().trim();
        String nombre = view.getTxtBuscarNombre().getText().trim();

        if (!idTexto.isEmpty()) {
            try {
                Funcionario f = funcionarioService.buscarPorId(Integer.parseInt(idTexto));
                model.setFuncionarios(f == null ? java.util.List.of() : java.util.List.of(f));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view.getPanel1(), "El ID debe ser numérico.");
            }
        } else if (!nombre.isEmpty()) {
            model.setFuncionarios(funcionarioService.buscarPorNombre(nombre));
        } else {
            model.setFuncionarios(funcionarioService.listarTodos());
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
                JOptionPane.showMessageDialog(view.getPanel1(), "Debe indicar el ID del funcionario.");
                return;
            }

            int id = Integer.parseInt(idTexto);
            Funcionario existente = funcionarioService.buscarPorId(id);

            if (existente == null) {
                Funcionario nuevo = new Funcionario(id, nombre, username, "", telefono);
                funcionarioService.crear(nuevo);
            } else {
                existente.setNombre(nombre);
                existente.setTelefono(telefono);
                existente.setUsername(username);
                funcionarioService.actualizar(existente);
            }

            model.setCurrent(null);
            model.setFuncionarios(funcionarioService.listarTodos());

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), "El ID debe ser numérico.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void borrar() {
        String idTexto = view.getTxtId().getText().trim();
        if (idTexto.isEmpty()) {
            JOptionPane.showMessageDialog(view.getPanel1(), "Seleccione un funcionario para borrar.");
            return;
        }
        try {
            int id = Integer.parseInt(idTexto);
            funcionarioService.eliminar(id);
            model.setCurrent(null);
            model.setFuncionarios(funcionarioService.listarTodos());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), "El ID debe ser numérico.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view.getPanel1(), ex.getMessage());
        }
    }

    private void limpiar() {
        model.setCurrent(null);
    }

    private void cargarSeleccion() {
        int fila = view.getTabla().getSelectedRow();
        if (fila < 0) return;

        DefaultTableModel tableModel = view.getTableModel();
        int id = Integer.parseInt(tableModel.getValueAt(fila, 0).toString());

        model.setCurrent(funcionarioService.buscarPorId(id));
    }
}