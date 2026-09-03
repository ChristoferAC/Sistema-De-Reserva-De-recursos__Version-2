package sistema.reservas.Calendarizacion;

import sistema.reservas.Categoria.CategoriaRecurso;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;


public class CalendarizacionPanel extends JPanel {

    private JTextField txtFecha;
    private JComboBox<CategoriaRecurso> comboCategoria;
    private JButton btnCargar;
    private JButton btnImprimir;
    private JTable tabla;
    private DefaultTableModel tableModel;

    public CalendarizacionPanel() {
        super(new BorderLayout(5, 5));
        construirInterfaz();
    }

    private void construirInterfaz() {
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));

        filtros.add(new JLabel("Fecha (AAAA-MM-DD):"));
        txtFecha = new JTextField(10);
        filtros.add(txtFecha);

        filtros.add(new JLabel("Categoría:"));
        comboCategoria = new JComboBox<>();
        comboCategoria.setPreferredSize(new Dimension(160, comboCategoria.getPreferredSize().height));
        // La categoría no tiene un toString() propio, así que se define
        // aquí cómo mostrarla en el combo (esto es solo vista, no toca
        // CategoriaRecurso.java de I1).
        comboCategoria.setRenderer((list, value, index, isSelected, cellHasFocus) ->
                new JLabel(value != null ? value.getNombre() : ""));
        filtros.add(comboCategoria);

        btnCargar = new JButton("Cargar");
        filtros.add(btnCargar);

        btnImprimir = new JButton("Imprimir");
        filtros.add(btnImprimir);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(tableModel);

        add(filtros, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
    }

    public JTextField getTxtFecha() {
        return txtFecha;
    }

    public JComboBox<CategoriaRecurso> getComboCategoria() {
        return comboCategoria;
    }

    public JButton getBtnCargar() {
        return btnCargar;
    }

    public JButton getBtnImprimir() {
        return btnImprimir;
    }

    public JTable getTabla() {
        return tabla;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }
}
