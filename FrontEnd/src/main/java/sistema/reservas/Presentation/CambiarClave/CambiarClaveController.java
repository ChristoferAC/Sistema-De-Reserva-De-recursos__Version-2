package sistema.reservas.Presentation.CambiarClave;

import sistema.reservas.Logic.Service;

public class CambiarClaveController {

    private final CambiarClaveView view;
    private final CambiarClaveModel model;

    public CambiarClaveController(CambiarClaveView view, CambiarClaveModel model) {
        this.view  = view;
        this.model = model;

        view.setModel(model);
        view.getBtnConfirmar().addActionListener(e -> cambiarClave());
    }

    private void cambiarClave() {
        String username       = view.getUsuario().trim();
        String claveActual    = new String(view.getClaveActual());
        String claveNueva     = new String(view.getClaveNueva());
        String claveConfirmar = new String(view.getClaveNuevaConfirmar());

        if (username.isBlank()) {
            model.setMensaje("Ingrese su usuario.");
            return;
        }

        if (claveNueva.isBlank()) {
            model.setMensaje("La clave nueva no puede estar vacía.");
            return;
        }

        if (!claveNueva.equals(claveConfirmar)) {
            model.setMensaje("Las claves nuevas no coinciden.");
            return;
        }

        try {
            // El Backend valida la clave actual y hace el cambio en una sola peticion
            Service.instance().cambiarClave(username, claveActual, claveNueva);
            model.setExito(true);
        } catch (Exception ex) {
            model.setMensaje(ex.getMessage());
        }
    }
}