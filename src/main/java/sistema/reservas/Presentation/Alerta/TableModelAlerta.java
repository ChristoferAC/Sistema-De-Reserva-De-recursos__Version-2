package sistema.reservas.Presentation.Alerta;

import sistema.reservas.Logic.Reserva;
import sistema.reservas.Presentation.AbstractTableModel;

import javax.swing.*;
import java.util.List;

public class TableModelAlerta extends AbstractTableModel<Reserva> implements javax.swing.table.TableModel {
    public TableModelAlerta(int[] cols, List<Reserva> rows) {
        super(cols, rows);
    }

    public static final int ACTIVIDAD = 0;
    public static final int VER = 1;
    public static final int DESCARTAR = 2;

    private static final Icon ICONO_VER = new ImageIcon(TableModelAlerta.class.getResource("/icons/ver.png"));
    private static final Icon ICONO_DESCARTAR = new ImageIcon(TableModelAlerta.class.getResource("/icons/descartar.png"));

    @Override
    protected void initColNames() {
        colNames = new String[3];
        colNames[ACTIVIDAD] = "Actividad";
        colNames[VER] = "Ver";
        colNames[DESCARTAR] = "Desc.";
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (cols[col]) {
            case VER:
            case DESCARTAR:
                return Icon.class;
            default:
                return super.getColumnClass(col);
        }
    }

    @Override
    protected Object getPropetyAt(Reserva e, int col) {
        switch (cols[col]) {
            case ACTIVIDAD:
                return e.getFuncionario().getNombre() + " - " + e.getActividad();
            case VER:
                return ICONO_VER;
            case DESCARTAR:
                return ICONO_DESCARTAR;
            default:
                return "";
        }
    }
}