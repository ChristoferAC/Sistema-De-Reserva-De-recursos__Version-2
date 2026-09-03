package sistema.reservas.Funcionario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class FuncionarioController {

    private final FuncionarioPanel view;
    private final FuncionarioService funcionarioService;

    public FuncionarioController(FuncionarioPanel view, FuncionarioService funcionarioService) {
        this.view = view;
        this.funcionarioService = funcionarioService;

        this.view.getBtnBuscar().addActionListener(e -> buscar());
        this.view.getBtnGuardar().addActionListener(e -> guardar());
        this.view.getBtnBorrar().addActionListener(e -> borrar());
        this.view.getBtnLimpiar().addActionListener(e -> view.limpiarFormulario());
        this.view.getTabla().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        cargarTabla(funcionarioService.listarTodos());
    }

    private void buscar() {
        String idTexto = view.getTxtBuscarId().getText().trim();
        String nombre = view.getTxtBuscarNombre().getText().trim();

        if (!idTexto.isEmpty()) {
            try {
                Funcionario f = funcionarioService.buscarPorId(Integer.parseInt(idTexto));
                cargarTabla(f == null ? List.of() : List.of(f));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "El ID debe ser numérico.");
            }
        } else if (!nombre.isEmpty()) {
            cargarTabla(funcionarioService.buscarPorNombre(nombre));
        } else {
            cargarTabla(funcionarioService.listarTodos());
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
                JOptionPane.showMessageDialog(view, "Debe indicar el ID del funcionario.");
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

            view.limpiarFormulario();
            cargarTabla(funcionarioService.listarTodos());

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "El ID debe ser numérico.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage());
        }
    }

    private void borrar() {
        String idTexto = view.getTxtId().getText().trim();
        if (idTexto.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Seleccione un funcionario para borrar.");
            return;
        }
        try {
            int id = Integer.parseInt(idTexto);
            funcionarioService.eliminar(id);
            view.limpiarFormulario();
            cargarTabla(funcionarioService.listarTodos());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "El ID debe ser numérico.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(view, ex.getMessage());
        }
    }

    private void cargarSeleccion() {
        int fila = view.getTabla().getSelectedRow();
        if (fila < 0) return;

        DefaultTableModel model = view.getTableModel();
        int id = Integer.parseInt(model.getValueAt(fila, 0).toString());
        String nombre = model.getValueAt(fila, 1).toString();
        String username = model.getValueAt(fila, 2).toString();
        String telefono = model.getValueAt(fila, 3).toString();

        view.cargarFormulario(id, nombre, username, telefono);
    }

    private void cargarTabla(List<Funcionario> funcionarios) {
        DefaultTableModel model = view.getTableModel();
        model.setRowCount(0);
        for (Funcionario f : funcionarios) {
            model.addRow(new Object[]{f.getId(), f.getNombre(), f.getUsername(), f.getTelefono()});
        }
    }
}