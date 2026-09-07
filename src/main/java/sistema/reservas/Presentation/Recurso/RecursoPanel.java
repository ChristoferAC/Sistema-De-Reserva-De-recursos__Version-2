package sistema.reservas.Presentation.Recurso;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class RecursoPanel implements PropertyChangeListener {
    private JPanel panel1;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JComboBox<String> cmbCategoria;
    private JTextField txtFiltroCategoria;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JButton btnCancelar;
    private JButton btnBuscar;

    private JTable tabla;
    private JScrollPane scrollTabla;

    private final List<CategoriaRecurso> categoriasDisponibles = new ArrayList<>();

    public RecursoPanel() {

        btnNuevo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiar();
            }
        });

        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                limpiar();
            }
        });

        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validate()) {
                    Recurso recurso = take();
                    try {
                        controller.crear(recurso);
                        JOptionPane.showMessageDialog(panel1, "RECURSO REGISTRADO", "", JOptionPane.INFORMATION_MESSAGE);
                        limpiar();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel1, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnEditar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validate()) {
                    Recurso recurso = take();
                    try {
                        controller.modificar(recurso);
                        JOptionPane.showMessageDialog(panel1, "RECURSO MODIFICADO", "", JOptionPane.INFORMATION_MESSAGE);
                        limpiar();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(panel1, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        btnEliminar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int fila = tabla.getSelectedRow();
                if (fila < 0) {
                    JOptionPane.showMessageDialog(panel1, "Seleccione un recurso.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                int respuesta = JOptionPane.showConfirmDialog(panel1, "¿Desea eliminar el recurso seleccionado?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

                if (respuesta != JOptionPane.YES_OPTION) {
                    return;
                }

                try {
                    String id = tabla.getValueAt(fila, 0).toString();
                    controller.eliminar(id);
                    JOptionPane.showMessageDialog(panel1, "RECURSO ELIMINADO", "", JOptionPane.INFORMATION_MESSAGE);
                    limpiar();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel1, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnBuscar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarRecursos();
            }
        });

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarRecursoSeleccionado();
            }
        });
    }

    public JPanel getPanel() {
        return panel1;
    }

    RecursoController controller;
    RecursoService service;

    public void setController(RecursoController controller) {
        this.controller = controller;
        cargarRecursos();
    }

    public void setService(RecursoService service) {
        this.service = service;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        cargarRecursos();
        panel1.revalidate();
        panel1.repaint();
    }

    public Recurso take() {
        Recurso recurso = new Recurso(
                txtId.getText().trim(),
                txtNombre.getText().trim(),
                txtDescripcion.getText().trim(),
                obtenerCategoriaSeleccionada());
        return recurso;
    }

    /**
     * Resuelve el item seleccionado en el combo hacia el objeto real que representa.
     */
    private CategoriaRecurso obtenerCategoriaSeleccionada() {
        int indice = cmbCategoria.getSelectedIndex();
        if (indice < 0 || indice >= categoriasDisponibles.size()) {
            return null;
        }
        return categoriasDisponibles.get(indice);
    }

    private boolean validate() {
        boolean valido = true;

        if (txtId.getText().trim().isEmpty()) {
            valido = false;
            txtId.setToolTipText("ID requerido");
        } else {
            txtId.setToolTipText(null);
        }

        if (txtNombre.getText().trim().isEmpty()) {
            valido = false;
            txtNombre.setToolTipText("Nombre requerido");
        } else {
            txtNombre.setToolTipText(null);
        }

        if (txtDescripcion.getText().trim().isEmpty()) {
            valido = false;
            txtDescripcion.setToolTipText("Descripción requerida");
        } else {
            txtDescripcion.setToolTipText(null);
        }

        if (cmbCategoria.getSelectedItem() == null) {
            valido = false;
            cmbCategoria.setToolTipText("Categoría requerida");
        } else {
            cmbCategoria.setToolTipText(null);
        }

        return valido;
    }

    private void cargarRecursos() {
        if (controller == null) {
            return;
        }

        try {
            List<Recurso> recursos = controller.listar();

            String filtro = txtFiltroCategoria.getText() == null
                    ? "" : txtFiltroCategoria.getText().trim().toLowerCase();

            String[] columnas = {"ID", "Nombre", "Descripción", "Categoría"};

            DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            for (Recurso recurso : recursos) {
                String categoria = recurso.getCategoria() != null
                        ? recurso.getCategoria().getDescripcion() : "";

                if (!filtro.isEmpty() && !categoria.toLowerCase().contains(filtro)) {
                    continue;
                }

                modelo.addRow(new Object[]{
                        recurso.getId(),
                        recurso.getNombre(),
                        recurso.getDescripcion(),
                        categoria
                });
            }

            tabla.setModel(modelo);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel1, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarRecursoSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;
        }

        txtId.setText(String.valueOf(tabla.getValueAt(fila, 0)));
        txtNombre.setText(String.valueOf(tabla.getValueAt(fila, 1)));
        txtDescripcion.setText(String.valueOf(tabla.getValueAt(fila, 2)));

        String descripcionCategoria = String.valueOf(tabla.getValueAt(fila, 3));
        for (int i = 0; i < categoriasDisponibles.size(); i++) {
            if (categoriasDisponibles.get(i).getDescripcion().equals(descripcionCategoria)) {
                cmbCategoria.setSelectedIndex(i);
                break;
            }
        }
    }

    public void limpiar() {
        txtId.setText("");
        txtNombre.setText("");
        txtDescripcion.setText("");
        cmbCategoria.setSelectedItem(null);
        tabla.clearSelection();
    }

    /**
     * Agrega una categoría real disponible; su descripción es lo que se muestra en el combo.
     */
    public void agregarCategoria(CategoriaRecurso categoria) {
        if (categoria == null || categoria.getDescripcion() == null || categoria.getDescripcion().trim().isEmpty()) {
            return;
        }
        categoriasDisponibles.add(categoria);
        cmbCategoria.addItem(categoria.getDescripcion());
    }

    public void limpiarCategorias() {
        categoriasDisponibles.clear();
        cmbCategoria.removeAllItems();
    }

    public CategoriaRecurso getCategoriaSeleccionada() {
        return obtenerCategoriaSeleccionada();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "Datos del Archivo", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 6, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(1, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnNuevo = new JButton();
        btnNuevo.setText("Nuevo");
        panel2.add(btnNuevo, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnGuardar = new JButton();
        btnGuardar.setText("Guardar");
        panel2.add(btnGuardar, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEditar = new JButton();
        btnEditar.setText("Editar");
        panel2.add(btnEditar, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEliminar = new JButton();
        btnEliminar.setText("Eliminar");
        panel2.add(btnEliminar, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCancelar = new JButton();
        btnCancelar.setText("Cancelar");
        panel2.add(btnCancelar, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(3, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(0, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("ID/Activo:");
        panel3.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtId = new JTextField();
        panel3.add(txtId, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Nombre:");
        panel3.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtNombre = new JTextField();
        panel3.add(txtNombre, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Descripcion:");
        panel3.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Categoria");
        panel3.add(label4, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtDescripcion = new JTextField();
        panel3.add(txtDescripcion, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        cmbCategoria = new JComboBox();
        panel3.add(cmbCategoria, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Filtar Categoria");
        panel3.add(label5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtFiltroCategoria = new JTextField();
        panel3.add(txtFiltroCategoria, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        btnBuscar = new JButton();
        btnBuscar.setText("Buscar");
        panel3.add(btnBuscar, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scrollTabla = new JScrollPane();
        scrollTabla.setEnabled(true);
        panel1.add(scrollTabla, new GridConstraints(2, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollTabla.setBorder(BorderFactory.createTitledBorder(null, "Recursos Registrados", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tabla = new JTable();
        scrollTabla.setViewportView(tabla);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}
