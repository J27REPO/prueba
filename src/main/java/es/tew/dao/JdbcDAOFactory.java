package es.tew.dao;

/**
 * Implementación concreta de DAOFactory utilizando JDBC.
 */
public class JdbcDAOFactory extends DAOFactory {

    @Override
    public UsuarioDAO getUsuarioDAO() {
        // Devolverá la implementación concreta del DAO de Usuario
        return new UsuarioDaoJdbc();
    }

    @Override  
    public IncidenciaDAO getIncidenciaDAO() {
        // Devuelve la implementación concreta del DAO de Incidencia
        return new IncidenciaDaoJdbc(); 
    }
    // Aquí irían las implementaciones de los demás DAOs:
    // @Override
    // public IncidenciaDAO getIncidenciaDAO() {
    //     return new IncidenciaDaoJdbc();
    // }

    @Override
    public ComentarioDAO getComentarioDAO() {
        // TODO Auto-generated method stub
        return new ComentarioDaoJdbc();
    }
    @Override 
    public HistorialEstadoDAO getHistorialEstadoDAO() {
        // Devuelve la implementación concreta del DAO de HistorialEstado
        return new HistorialEstadoDaoJdbc(); 
    }
}