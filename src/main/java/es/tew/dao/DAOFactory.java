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
    // MODIFICADO: Usar BDD en memoria para evitar problemas de ruta/permisos
    private static final String URL = "jdbc:hsqldb:mem:incidenciasDB";
    private static final String USER = "SA";
    private static final String PASSWORD = "";
    
    private static Connection connection = null;

    /**
     * Lógica de inicialización de la conexión y carga de datos.
     * Se llama al inicio de la aplicación y cada vez que la conexión se cierra.
     */
    private static void initializeDatabase() {
        try {
            // 1. Cargar el driver
            Class.forName(DRIVER);

            // 2. Establecer la conexión (recrea la DB en memoria si ya se había cerrado)
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            
            // 3. Ejecutar el script de inicialización para cargar datos.
            executeScript("data/LocalDB.script");
            
            System.out.println("INFO: Base de datos HSQLDB inicializada/reinicializada con éxito.");
            
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: No se pudo cargar el driver de HSQLDB. ¿Falta el JAR en el pom.xml?");
            e.printStackTrace();
            connection = null; 
        } catch (SQLException e) {
            System.err.println("ERROR al conectar con la base de datos o ejecutar el script inicial.");
            e.printStackTrace();
            connection = null;
        }
    }

    /**
     * Inicializa la factoría al inicio del deploy.
     */
    static {
        initializeDatabase();
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
            }
        } catch (IOException e) {
            System.err.println("ERROR: No se pudo leer el script de BDD.");
            e.printStackTrace();
        }
    }

    /**
     * Proporciona la conexión JDBC a los DAOs, reinicializando la DB si se ha cerrado.
     * @return La conexión JDBC.
     */
    public static Connection getConnection() {
        try {
            // Lógica de resiliencia: Comprueba si la conexión es nula o ha sido cerrada
            if (connection == null || connection.isClosed()) {
                System.out.println("ADVERTENCIA: Conexión HSQLDB cerrada/nula. Reinicializando DB...");
                initializeDatabase(); // Recrea la DB y recarga los datos
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar el estado de la conexión: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
    
    public abstract IncidenciaDAO getIncidenciaDAO();
    public abstract ComentarioDAO getComentarioDAO();
    public abstract HistorialEstadoDAO getHistorialEstadoDAO();

    public abstract UsuarioDAO getUsuarioDAO();
    
    /**
     * Obtiene la instancia de la Factoría. Por ahora, solo tendremos una implementación.
     */
    public static DAOFactory getFactory() {
        return new JdbcDAOFactory();
    }
}