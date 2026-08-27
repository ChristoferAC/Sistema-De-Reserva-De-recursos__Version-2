package sistema.reservas.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TablaCrudPanel extends JPanel {

    protected DefaultTableModel tableModel;
    protected JTable tabla;
    protected JButton btnNuevo;
    protected JButton btnEditar;
    protected JButton btnEliminar;
    protected JButton btnActualizar;

    public TablaCrudPanel(String[] columnas) {
        super(new BorderLayout(5, 5));
        construirInterfaz(columnas);
    }

    private void construirInterfaz(String[] columnas) {
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // La edicion real se hace por formulario (boton "Editar"),
                // no directamente en la celda de la tabla.
                return false;
            }
        };
        tabla = new JTable(tableModel);
        tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnNuevo = new JButton("Nuevo");
        btnEditar = new JButton("Editar");
        btnEliminar = new JButton("Eliminar");
        btnActualizar = new JButton("Actualizar");
        toolbar.add(btnNuevo);
        toolbar.add(btnEditar);
        toolbar.add(btnEliminar);
        toolbar.add(btnActualizar);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    public JTable getTabla() {
        return tabla;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public JButton getBtnNuevo() {
        return btnNuevo;
    }

    public JButton getBtnEditar() {
        return btnEditar;
    }

    public JButton getBtnEliminar() {
        return btnEliminar;
    }

    public JButton getBtnActualizar() {
        return btnActualizar;
    }
}
