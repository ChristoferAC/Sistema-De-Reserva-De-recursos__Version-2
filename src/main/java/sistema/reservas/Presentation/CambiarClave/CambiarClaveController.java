package sistema.reservas.Presentation.CambiarClave;

import sistema.reservas.Logic.Usuario;
import sistema.reservas.Logic.Services.UsuarioService;

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
            UsuarioService service = new UsuarioService();
            // Primero validamos que el usuario y clave actual sean correctos
            Usuario usuario = service.login(username, claveActual);
            // Si llegamos aquí, las credenciales son correctas — cambiamos
            service.cambiarClave(usuario, claveActual, claveNueva);
            model.setExito(true);
        } catch (Exception ex) {
            model.setMensaje(ex.getMessage());
        }
    }
}