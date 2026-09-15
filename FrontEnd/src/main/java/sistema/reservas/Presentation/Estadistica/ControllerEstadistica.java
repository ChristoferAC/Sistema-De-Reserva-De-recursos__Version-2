package sistema.reservas.Presentation.Estadistica;

import sistema.reservas.Presentation.Estadistica.Service.ServiceEstadistica;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Controller de Estadísticas. Responsable: Integrante 3.
 *
 * Ya NO pinta tabla ni gráfico directamente: le pide el resultado al
 * ServiceEstadistica y se lo entrega al Model (model.setPorCategoria(...)
 * / model.setPorSemana(...)). Es la View, escuchando el Model vía
 * PropertyChangeListener, la que se encarga de pintarse sola.
 */
public class ControllerEstadistica {

    private final ViewEstadistica view;
    private final ModelEstadistica model;
    private final ServiceEstadistica service;

    public ControllerEstadistica(ViewEstadistica view, ModelEstadistica model, ServiceEstadistica service) {
        this.view = view;
        this.model = model;
        this.service = service;

        view.setModel(model);

        view.getBtnCargarRecurso().addActionListener(e -> onCargarRecursos());
        view.getBtnCargarActividad().addActionListener(e -> onCargarActividades());
    }

    private void onCargarRecursos() {
        try {
            LocalDate desde = parsearFecha(view.getTxtDesdeRecurso().getText());
            LocalDate hasta = parsearFecha(view.getTxtHastaRecurso().getText());

            ResultadoEstadistica resultado = service.contarPorCategoria(desde, hasta);
            model.setPorCategoria(resultado); // <-- antes aquí se llamaba pintarTabla+pintarGrafico directo

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

            ResultadoEstadistica resultado = service.contarPorSemana(desde, hasta);
            model.setPorSemana(resultado);

        } catch (DateTimeParseException ex) {
            mostrarError("Fecha inválida. Use el formato AAAA-MM-DD.");
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private LocalDate parsearFecha(String texto) {
        return LocalDate.parse(texto.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(view.getEstadistica(), mensaje);
    }
}