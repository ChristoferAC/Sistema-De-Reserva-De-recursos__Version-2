package sistema.reservas.Presentation.Estadistica;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import sistema.reservas.Presentation.Estadistica.Services.ServiceEstadistica;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Controller de Estadísticas. Responsable: Integrante 3.
 *
 * La pantalla tiene 2 bloques independientes (Recursos y Actividades),
 * cada uno con su propio rango de fechas, tabla y gráfico — pero los
 * dos llaman al mismo ServiceEstadistica, solo que a un método
 * distinto (contarPorCategoria vs contarPorSemana).
 */
public class ControllerEstadistica {

    private final ViewEstadistica view;
    private final ServiceEstadistica service;

    public ControllerEstadistica(ViewEstadistica view, ServiceEstadistica service) {
        this.view = view;
        this.service = service;

        view.getBtnCargarRecurso().addActionListener(e -> onCargarRecursos());
        view.getBtnCargarActividad().addActionListener(e -> onCargarActividades());
    }

    private void onCargarRecursos() {
        try {
            LocalDate desde = parsearFecha(view.getTxtDesdeRecurso().getText());
            LocalDate hasta = parsearFecha(view.getTxtHastaRecurso().getText());

            ModelEstadistica modelo = service.contarPorCategoria(desde, hasta);

            pintarTabla(view.getTableModelRecursos(), "Categoria", modelo);
            pintarGrafico(view.getPanelGraficoRecursos(), "Recursos Usados", "Recurso", modelo);

        } catch (DateTimeParseException ex) {
            mostrarError("Fecha inválida. Use el formato AAAA-MM-DD.");
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void onCargarActividades() {
        try {
            LocalDate desde = parsearFecha(view.getTxtDesdeActividad().getText());
            LocalDate hasta = parsearFecha(view.getTxtHastaActividad().getText());

            ModelEstadistica modelo = service.contarPorSemana(desde, hasta);

            pintarTabla(view.getTableModelActividades(), "Semana", modelo);
            pintarGrafico(view.getPanelGraficoActividades(), "Actividades Realizadas", "Semana", modelo);

        } catch (DateTimeParseException ex) {
            mostrarError("Fecha inválida. Use el formato AAAA-MM-DD.");
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private LocalDate parsearFecha(String texto) {
        return LocalDate.parse(texto.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void pintarTabla(DefaultTableModel modelo, String nombreColumnaEtiqueta, ModelEstadistica datos) {
        modelo.setRowCount(0);
        modelo.setColumnCount(0);
        modelo.addColumn(nombreColumnaEtiqueta);
        modelo.addColumn("Cantidad");

        List<String> etiquetas = datos.getEtiquetas();
        List<Integer> cantidades = datos.getCantidades();
        for (int i = 0; i < etiquetas.size(); i++) {
            modelo.addRow(new Object[]{etiquetas.get(i), cantidades.get(i)});
        }
    }

    /**
     * Arma un gráfico de barras (igual al de la imagen del enunciado) y
     * lo mete dentro del JPanel vacío que ya está en la Vista, siguiendo
     * la guía: crear el JFreeChart, envolverlo en un ChartPanel, limpiar
     * el JPanel y agregarle el ChartPanel.
     */
    private void pintarGrafico(JPanel contenedor, String titulo, String nombreSerie, ModelEstadistica datos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        List<String> etiquetas = datos.getEtiquetas();
        List<Integer> cantidades = datos.getCantidades();
        for (int i = 0; i < etiquetas.size(); i++) {
            dataset.addValue(cantidades.get(i), nombreSerie, etiquetas.get(i));
        }

        JFreeChart chart = ChartFactory.createBarChart(titulo, "", "Cantidad", dataset);
        ChartPanel chartPanel = new ChartPanel(chart);

        contenedor.removeAll();
        contenedor.setLayout(new BorderLayout());
        contenedor.add(chartPanel, BorderLayout.CENTER);
        contenedor.revalidate();
        contenedor.repaint();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(view.getEstadistica(), mensaje);
    }
}