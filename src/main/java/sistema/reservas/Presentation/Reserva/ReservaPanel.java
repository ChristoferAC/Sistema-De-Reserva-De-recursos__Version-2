package sistema.reservas.Presentation.Reserva;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import sistema.reservas.Logic.Funcionario;
import sistema.reservas.Logic.Recurso;
import sistema.reservas.Logic.Reserva;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ReservaPanel implements PropertyChangeListener {

    private JPanel panel1;
    private JLabel lblFuncionario;
    private JTextField txtId;
    private JTextField txtActividad;
    private JTextField txtFecha;
    private JTextField txtHoraInicio;
    private JTextField txtHoraFin;
    private JComboBox<String> listaRecursos;
    private JButton btnNueva;
    private JButton btnReservar;
    private JButton btnEditar;
    private JButton btnCancelar;
    private JButton btnLimpiar;
    private JButton btnUsarIA;
    private JTable tablaReservas;

    private DefaultTableModel tableModel;
    private Funcionario funcionarioActual;

    // MVC
    private ReservaModel model;

    public ReservaPanel() {
        tableModel = new DefaultTableModel(
                new String[]{"ID", "Funcionario", "Actividad", "Fecha", "Hora Inicio", "Hora Fin", "Recursos"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaReservas.setModel(tableModel);
    }

    public JPanel getPanel() {
        return panel1;
    }

    // --- MVC: enlace con el Model ---

    public void setModel(ReservaModel model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case ReservaModel.RESERVAS:
                actualizarTabla(model.getReservas());
                break;
            case ReservaModel.RECURSOS:
                actualizarListaRecursos(model.getRecursos());
                break;
            case ReservaModel.CURRENT:
                Reserva actual = model.getCurrent();
                if (actual == null) {
                    limpiarFormulario();
                } else {
                    cargarFormulario(actual);
                }
                break;
        }
    }

    private void actualizarTabla(List<Reserva> reservas) {
        tableModel.setRowCount(0);
        for (Reserva reserva : reservas) {
            String nombreFuncionario = reserva.getFuncionario() != null ? reserva.getFuncionario().getNombre() : "";
            tableModel.addRow(new Object[]{
                    reserva.getId(),
                    nombreFuncionario,
                    reserva.getActividad(),
                    reserva.getFecha(),
                    reserva.getHoraInicio(),
                    reserva.getHoraFin(),
                    obtenerRecursos(reserva)
            });
        }
    }

    private String obtenerRecursos(Reserva reserva) {
        if (reserva.getRecursosAsignados() == null) {
            return "";
        }
        StringBuilder texto = new StringBuilder();
        List<Recurso> recursos = reserva.getRecursosAsignados();
        for (int i = 0; i < recursos.size(); i++) {
            Recurso recurso = recursos.get(i);
            if (recurso == null) continue;
            texto.append(recurso.getNombre());
            if (i < recursos.size() - 1) {
                texto.append(", ");
            }
        }
        return texto.toString();
    }

    private void actualizarListaRecursos(List<Recurso> recursos) {
        String seleccionActual = (String) listaRecursos.getSelectedItem();
        listaRecursos.removeAllItems();
        for (Recurso recurso : recursos) {
            listaRecursos.addItem(descripcionRecurso(recurso));
        }
        if (seleccionActual != null) {
            listaRecursos.setSelectedItem(seleccionActual);
        }
    }

    private String descripcionRecurso(Recurso recurso) {
        String categoria = recurso.getCategoria() != null ? recurso.getCategoria().getDescripcion() : "";
        return recurso.getNombre() + " (" + categoria + ")";
    }

    /**
     * Limpia el formulario para cargar una reserva nueva.
     */
    public void limpiarFormulario() {
        txtId.setText("");
        txtActividad.setText("");
        txtFecha.setText("");
        txtHoraInicio.setText("");
        txtHoraFin.setText("");
        listaRecursos.setSelectedItem(null);
        tablaReservas.clearSelection();
    }

    /**
     * Carga los datos de una reserva seleccionada en el formulario.
     */
    public void cargarFormulario(Reserva reserva) {
        txtId.setText(String.valueOf(reserva.getId()));
        lblFuncionario.setText(reserva.getFuncionario() != null ? reserva.getFuncionario().getNombre() : "");
        txtActividad.setText(reserva.getActividad());
        txtFecha.setText(reserva.getFecha() != null ? reserva.getFecha().toString() : "");
        txtHoraInicio.setText(reserva.getHoraInicio() != null ? reserva.getHoraInicio().toString() : "");
        txtHoraFin.setText(reserva.getHoraFin() != null ? reserva.getHoraFin().toString() : "");
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionarioActual = funcionario;
        lblFuncionario.setText(funcionario != null ? funcionario.getNombre() : "");
    }

    public Funcionario getFuncionario() {
        return funcionarioActual;
    }

    // --- Getters usados por el Controller ---

    public JLabel getLblFuncionario() {
        return lblFuncionario;
    }

    public JTextField getTxtId() {
        return txtId;
    }

    public JTextField getTxtActividad() {
        return txtActividad;
    }

    public JTextField getTxtFecha() {
        return txtFecha;
    }

    public JTextField getTxtHoraInicio() {
        return txtHoraInicio;
    }

    public JTextField getTxtHoraFin() {
        return txtHoraFin;
    }

    public JComboBox<String> getListaRecursos() {
        return listaRecursos;
    }

    public JButton getBtnNueva() {
        return btnNueva;
    }

    public JButton getBtnReservar() {
        return btnReservar;
    }

    public JButton getBtnEditar() {
        return btnEditar;
    }

    public JButton getBtnCancelar() {
        return btnCancelar;
    }

    public JButton getBtnLimpiar() {
        return btnLimpiar;
    }

    public JButton getBtnUsarIA() {
        return btnUsarIA;
    }

    public JTable getTablaReservas() {
        return tablaReservas;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
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
        panel1.setLayout(new GridLayoutManager(6, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.setBorder(BorderFactory.createTitledBorder(null, "Nueva Reserva", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.BELOW_TOP, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Funcionario:");
        panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        lblFuncionario = new JLabel();
        lblFuncionario.setText("");
        panel1.add(lblFuncionario, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("ID Reserva:");
        panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtId = new JTextField();
        panel1.add(txtId, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Fecha:");
        panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Hora Finalizacion:");
        panel1.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtFecha = new JTextField();
        txtFecha.setText("");
        panel1.add(txtFecha, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtHoraFin = new JTextField();
        panel1.add(txtHoraFin, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Actividad:");
        panel1.add(label5, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Hora Inicio:");
        panel1.add(label6, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Recursos");
        panel1.add(label7, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        txtActividad = new JTextField();
        panel1.add(txtActividad, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        txtHoraInicio = new JTextField();
        panel1.add(txtHoraInicio, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 7, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(4, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        btnNueva = new JButton();
        btnNueva.setText("Nueva");
        panel2.add(btnNueva, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel2.add(spacer2, new GridConstraints(0, 6, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        btnReservar = new JButton();
        btnReservar.setText("Registrar reserva");
        panel2.add(btnReservar, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnEditar = new JButton();
        btnEditar.setText("Modificar");
        panel2.add(btnEditar, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCancelar = new JButton();
        btnCancelar.setText("Cancelar Reserva");
        panel2.add(btnCancelar, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnLimpiar = new JButton();
        btnLimpiar.setText("Limpiar");
        panel2.add(btnLimpiar, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnUsarIA = new JButton();
        btnUsarIA.setText("Usar IA");
        panel2.add(btnUsarIA, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        listaRecursos = new JComboBox();
        panel1.add(listaRecursos, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new GridConstraints(5, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(null, "Reservas", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tablaReservas = new JTable();
        scrollPane1.setViewportView(tablaReservas);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }

}