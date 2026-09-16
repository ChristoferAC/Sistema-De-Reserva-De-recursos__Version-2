package sistema.reservas.Presentation.CambiarClave;

import sistema.reservas.Logic.Usuario;
import sistema.reservas.Logic.Services.UsuarioService;

public class CambiarClaveController {

    private final CambiarClaveView view;
    private final CambiarClaveModel model;
    private final Usuario usuario;

    public CambiarClaveController(CambiarClaveView view, CambiarClaveModel model, Usuario usuario) {
        this.view    = view;
        this.model   = model;
        this.usuario = usuario;

        // El Controller conecta el botón — no la View ni nadie más
        view.setModel(model);
        view.getBtnConfirmar().addActionListener(e -> cambiarClave());
    }

    private void cambiarClave() {
        String claveActual    = new String(view.getClaveActual());
        String claveNueva     = new String(view.getClaveNueva());
        String claveConfirmar = new String(view.getClaveNuevaConfirmar());

        if (claveNueva.isBlank()) {
            model.setMensaje("La clave nueva no puede estar vacía.");
            return;
        }

        if (!claveNueva.equals(claveConfirmar)) {
            model.setMensaje("Las claves nuevas no coinciden.");
            return;
        }

        try {
            UsuarioService service = new UsuarioService();
            service.cambiarClave(usuario, claveActual, claveNueva);
            // Notifica éxito → View reacciona en propertyChange("exito") y se cierra
            model.setExito(true);
        } catch (Exception ex) {
            model.setMensaje(ex.getMessage());
        }
    }
}