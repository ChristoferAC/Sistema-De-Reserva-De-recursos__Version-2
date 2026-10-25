package sistema.reservas.Data.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fabrica de conexiones a MySQL (Integrante 3).
 *
 * NO guarda una conexion unica compartida: el Backend atiende varios clientes a la vez
 * (un Worker por cliente), asi que cada Worker pide SU propia conexion con getConnection()
 * y la cierra al terminar.
 *
 * Donde busca database.properties (en este orden):
 *   1. En la carpeta desde donde se ejecuta el programa.
 *   2. En el classpath (src/main/resources/database.properties).
 */
public class DataBase {

    public static final String PROPERTIES_FILE_NAME = "database.properties";

    private static DataBase theInstance;

    public static synchronized DataBase instance() {
        if (theInstance == null) {
            theInstance = new DataBase(cargarPropiedades());
        }
        return theInstance;
    }

    private final String url;
    private final String user;
    private final String password;

    public DataBase(Properties prop) {
        String driver = prop.getProperty("database_driver", "com.mysql.cj.jdbc.Driver");
        String server = obligatoria(prop, "database_server");
        String port = prop.getProperty("database_port", "3306");
        String name = obligatoria(prop, "database_name");
        this.user = obligatoria(prop, "database_user");
        this.password = prop.getProperty("database_password", "");

        this.url = "jdbc:mysql://" + server + ":" + port + "/" + name
                + "?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8";

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontro el driver de MySQL (" + driver
                    + "). Revise que mysql-connector-j este en el pom del BackEnd.", e);
        }
    }

    /** Abre una conexion NUEVA. Quien la pide es responsable de cerrarla. */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private static String obligatoria(Properties prop, String clave) {
        String valor = prop.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta '" + clave + "' en " + PROPERTIES_FILE_NAME + ".");
        }
        return valor.trim();
    }

    private static Properties cargarPropiedades() {
        Properties prop = new Properties();
        File externo = new File(PROPERTIES_FILE_NAME);
        try {
            if (externo.exists()) {
                try (InputStream in = new FileInputStream(externo)) {
                    prop.load(in);
                }
            } else {
                try (InputStream in = DataBase.class.getResourceAsStream("/" + PROPERTIES_FILE_NAME)) {
                    if (in == null) {
                        throw new IllegalStateException("No se encontro " + PROPERTIES_FILE_NAME
                                + " (se busco en " + externo.getAbsolutePath() + " y en el classpath). "
                                + "Copie database.properties como " + PROPERTIES_FILE_NAME
                                + " y ajuste sus datos de MySQL.");
                    }
                    prop.load(in);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + PROPERTIES_FILE_NAME + ": " + e.getMessage(), e);
        }
        return prop;
    }
}