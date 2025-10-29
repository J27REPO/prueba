package es.tew.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DAOFactory {

    private static final String DRIVER = "org.hsqldb.jdbcDriver";
    private static final String URL = "jdbc:hsqldb:mem:incidenciasDB;sql.syntax_ora=true";
    private static final String USER = "SA";
    private static final String PASSWORD = "";
    private static Connection connection = null;

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR CRÍTICO: No se pudo cargar el driver de HSQLDB.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            initializeDatabase();
        }
        return connection;
    }

    private static void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            executeScript("data/LocalDB.script");
        } catch (Exception e) {
            System.err.println("ERROR FATAL al inicializar la BDD: " + e.getMessage());
            connection = null;
            throw new RuntimeException(e);
        }
    }

    private static void executeScript(String scriptPath) throws SQLException, IOException {
        try (InputStream is = DAOFactory.class.getClassLoader().getResourceAsStream(scriptPath)) {
            if (is == null) {
                throw new IOException("No se encontró el script de BDD en: " + scriptPath);
            }
            String sqlScript = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // Dividimos el script en sentencias individuales usando el punto y coma como delimitador
            String[] statements = sqlScript.split(";");
            try (Statement stmt = connection.createStatement()) {
                // Ejecutamos cada sentencia una por una
                for (String statement : statements) {
                    String trimmedStatement = statement.trim();
                    if (!trimmedStatement.isEmpty()) {
                        stmt.execute(trimmedStatement);
                    }
                }
            }
        }
    }
    
    // --- Métodos abstractos para obtener los DAOs ---
    public abstract UsuarioDAO getUsuarioDAO();
    public abstract IncidenciaDAO getIncidenciaDAO();
    public abstract ComentarioDAO getComentarioDAO();
    public abstract HistorialEstadoDAO getHistorialEstadoDAO();

    public static DAOFactory getFactory() {
        return new JdbcDAOFactory();
    }
}