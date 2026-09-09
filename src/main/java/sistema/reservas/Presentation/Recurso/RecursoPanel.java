package sistema.reservas.Presentation.Recurso;

import sistema.reservas.Logic.CategoriaRecurso;
import sistema.reservas.Logic.Recurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class RecursoPanel implements PropertyChangeListener{
    private JPanel panel1;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JComboBox<String>  cmbCategoria;
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

    /** Resuelve el item seleccionado en el combo hacia el objeto real que representa. */
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

    /** Agrega una categoría real disponible; su descripción es lo que se muestra en el combo. */
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
}
