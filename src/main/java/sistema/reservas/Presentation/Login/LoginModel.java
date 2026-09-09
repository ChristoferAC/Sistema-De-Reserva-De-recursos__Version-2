package sistema.reservas.Presentation.Login;

import sistema.reservas.Logic.Usuario;
import sistema.reservas.Presentation.AbstractModel;

/**
 * Model del MVC de Login.
 * Guarda el Usuario que quedo logueado y notifica a la View cuando
 * cambia (para que la View sepa que el login fue exitoso, por ejemplo).
 */
public class LoginModel extends AbstractModel {

    public static final String CURRENT = "current";

    private Usuario current;

    public LoginModel() {
        // Usuario es abstracta en este proyecto (a diferencia del
        // ejemplo), asi que arrancamos sin usuario logueado (null)
        // en vez de "new Usuario()".
        current = null;
    }

    public Usuario getCurrent() {
        return current;
    }

    public void setCurrent(Usuario current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }
}
