package sistema.reservas.Data.persistence;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Serializa/des-serializa las Categorias de Recurso a/desde XML usando
 * JAXB. Sin DAO de por medio: el Service llama esta clase directamente.
 */
public class CategoriaXmlPersister {

    private static final String PATH = "data/categorias.xml";

    public CategoriasData load() throws Exception {
        File archivo = new File(PATH);
        if (!archivo.exists()) {
            return new CategoriasData();
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(CategoriasData.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        try (FileInputStream is = new FileInputStream(archivo)) {
            return (CategoriasData) unmarshaller.unmarshal(is);
        }
    }

    public void store(CategoriasData data) throws Exception {
        File archivo = new File(PATH);
        File carpeta = archivo.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        JAXBContext jaxbContext = JAXBContext.newInstance(CategoriasData.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        try (FileOutputStream os = new FileOutputStream(archivo)) {
            marshaller.marshal(data, os);
        }
    }
}
