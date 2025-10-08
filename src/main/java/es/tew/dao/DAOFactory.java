package es.tew.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Clase Factoria de DAOs.
 * Implementa el patrón Singleton para la conexión de la BDD (solo una vez).
 */
public abstract class DAOFactory {

    private static final String DRIVER = "org.hsqldb.jdbcDriver";
    private static final String URL = "jdbc:hsqldb:file:./data/LocalDB";
    private static final String USER = "SA";
    private static final String PASSWORD = "";
    
    private static Connection connection = null;

    /**
     * Inicializa la factoría y la conexión de la base de datos.
     * Se llama al inicio de la aplicación para asegurar que el driver está cargado 
     * y el script inicial de la BDD se ejecuta si es necesario.
     */
    static {
        try {
            // 1. Cargar el driver
            Class.forName(DRIVER);

            // 2. Establecer la conexión
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            
            // 3. Ejecutar el script de inicialización si la BDD está vacía.
            // Esto es crucial para HSQLDB en modo archivo.
            executeScript("data/LocalDB.script");
            
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No se pudo cargar el driver de HSQLDB. ¿Falta el JAR en el pom.xml?");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("ERROR al conectar con la base de datos o ejecutar el script inicial.");
            e.printStackTrace();
        }
    }

    /**
     * Método interno para ejecutar un script SQL de inicialización.
     * @param scriptPath La ruta del script dentro de 'src/main/resources/'.
     * @throws SQLException Si hay un error SQL.
     */
    private static void executeScript(String scriptPath) throws SQLException {
        // Usamos un InputStream para cargar el script desde el classpath (src/main/resources)
        try (InputStream is = DAOFactory.class.getClassLoader().getResourceAsStream(scriptPath)) {
            if (is == null) {
                System.err.println("ADVERTENCIA: No se encontró el script de BDD en " + scriptPath);
                return;
            }

            // Lectura del script (usamos un buffer simple para HSQLDB)
            String sql = new String(is.readAllBytes());

            // Separar las sentencias por el punto y coma (;)
            String[] statements = sql.split(";");

            try (Statement stmt = connection.createStatement()) {
                for (String statement : statements) {
                    String trimmedStatement = statement.trim();
                    if (!trimmedStatement.isEmpty()) {
                        stmt.execute(trimmedStatement);
                    }
                }
                System.out.println("INFO: Script de base de datos ejecutado con éxito.");
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo leer el script de BDD.");
            e.printStackTrace();
        }
    }

    /**
     * Proporciona la conexión JDBC a los DAOs.
     * @return La conexión JDBC.
     */
    public static Connection getConnection() {
        return connection;
    }
    
    public abstract IncidenciaDAO getIncidenciaDAO();
    public abstract ComentarioDAO getComentarioDAO();
    public abstract HistorialEstadoDAO getHistorialEstadoDAO();
    // **********************************************
    // Métodos abstractos para obtener los DAOs
    // **********************************************

    public abstract UsuarioDAO getUsuarioDAO();
    
    // public abstract IncidenciaDAO getIncidenciaDAO();
    // public abstract ComentarioDAO getComentarioDAO();
    // public abstract HistorialEstadoDAO getHistorialEstadoDAO();

    /**
     * Obtiene la instancia de la Factoría. Por ahora, solo tendremos una implementación.
     */
    public static DAOFactory getFactory() {
        // Devuelve la implementación concreta de la factoría.
        return new JdbcDAOFactory();
    }
}