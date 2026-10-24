package sistema.reservas.Presentation.CambiarClave;

import sistema.reservas.Presentation.AbstractModel;

public class CambiarClaveModel extends AbstractModel {

    public static final String MENSAJE = "mensaje";
    public static final String EXITO   = "exito";

    private String  mensaje;
    private boolean exito;

    public CambiarClaveModel() {
        mensaje = " ";
        exito   = false;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
        firePropertyChange(MENSAJE);
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
        firePropertyChange(EXITO);
    }
}