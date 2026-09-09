package sistema.reservas.Data.persistence;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Serializa/des-serializa los Usuarios a/desde XML usando JAXB
 * (marshal/unmarshal), tal como lo explica el profesor. Sin DAO de
 * por medio: el Service llama esta clase directamente.
 */
public class UsuarioXmlPersister {

    private static final String PATH = "data/usuarios.xml";

    public UsuariosData load() throws Exception {
        File archivo = new File(PATH);
        if (!archivo.exists()) {
            // Primera vez que corre el programa: todavia no hay archivo.
            return new UsuariosData();
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(UsuariosData.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        try (FileInputStream is = new FileInputStream(archivo)) {
            return (UsuariosData) unmarshaller.unmarshal(is);
        }
    }

    public void store(UsuariosData data) throws Exception {
        File archivo = new File(PATH);
        File carpeta = archivo.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(UsuariosData.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        try (FileOutputStream os = new FileOutputStream(archivo)) {
            marshaller.marshal(data, os);
        }
    }
}
