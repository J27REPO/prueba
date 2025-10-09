package es.tew.web;

import es.tew.dao.DAOFactory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * ServletContextListener para inicializar y destruir la conexión a la BDD
 * una sola vez durante el ciclo de vida de la aplicación.
 */
@WebListener
public class DatabaseInitializerListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("INFO: Inicializando la conexión de la base de datos para toda la aplicación...");
        try {
            // Forzamos la inicialización de la BDD desde el DAOFactory
            Connection conn = DAOFactory.getConnection();
            System.out.println("INFO: Conexión a la base de datos establecida con éxito.");
        } catch (SQLException e) {
            System.err.println("ERROR CRÍTICO: No se pudo inicializar la conexión a la base de datos.");
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("INFO: Cerrando la conexión de la base de datos...");
        try {
            Connection conn = DAOFactory.getConnection();
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("INFO: Conexión a la base de datos cerrada correctamente.");
            }
        } catch (SQLException e) {
            System.err.println("ERROR: No se pudo cerrar la conexión a la base de datos.");
            e.printStackTrace();
        }
    }
}