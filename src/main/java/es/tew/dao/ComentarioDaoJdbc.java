package es.tew.dao;

import es.tew.dto.ComentarioDTO;
import es.tew.dto.IncidenciaDTO;
import es.tew.dto.UsuarioDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de ComentarioDAO utilizando JDBC.
 */
public class ComentarioDaoJdbc implements ComentarioDAO {

    private final DAOFactory factory = DAOFactory.getFactory();
    private final IncidenciaDAO incidenciaDAO = factory.getIncidenciaDAO();
    private final UsuarioDAO usuarioDAO = factory.getUsuarioDAO();

    // Nombres de columnas de la tabla COMENTARIO
    private static final String ID = "ID";
    private static final String ID_INCIDENCIA = "ID_INCIDENCIA";
    private static final String DNI_AUTOR = "DNI_AUTOR";
    private static final String TEXTO = "TEXTO";
    private static final String FECHA = "FECHA";

    // ******************************************************
    // UTILERÍA: Método para mapear un ResultSet a un ComentarioDTO
    // ******************************************************
    
    /**
     * Convierte una fila de la tabla COMENTARIO en un objeto ComentarioDTO,
     * resolviendo las relaciones con IncidenciaDTO y UsuarioDTO.
     * @param rs El ResultSet posicionado en una fila.
     * @return Un ComentarioDTO completo.
     * @throws SQLException Si ocurre un error al acceder a la columna.
     */
    private ComentarioDTO mapRowToDTO(ResultSet rs) throws SQLException {
        ComentarioDTO com = new ComentarioDTO();
        com.setId(rs.getLong(ID));
        com.setTexto(rs.getString(TEXTO));
        com.setFecha(rs.getTimestamp(FECHA));

        // 1. Resolver relación con Incidencia
        Long idIncidencia = rs.getLong(ID_INCIDENCIA);
        if (idIncidencia != 0) { // El ID siempre será > 0 si existe
            // Reutilizamos el IncidenciaDAO
            IncidenciaDTO incidencia = incidenciaDAO.findById(idIncidencia);
            com.setIncidencia(incidencia);
        }

        // 2. Resolver relación con Autor
        String dniAutor = rs.getString(DNI_AUTOR);
        if (dniAutor != null) {
            // Reutilizamos el UsuarioDAO
            UsuarioDTO autor = usuarioDAO.findByDni(dniAutor);
            com.setAutor(autor);
        }

        return com;
    }

    // ******************************************************
    // 1. CONSULTA ESPECÍFICA DEL NEGOCIO
    // ******************************************************

    @Override
    public List<ComentarioDTO> findByIncidenciaId(Long idIncidencia) {
        List<ComentarioDTO> comentarios = new ArrayList<>();
        // Ordenamos por fecha ascendente para ver el hilo de conversación
        String sql = "SELECT * FROM COMENTARIO WHERE ID_INCIDENCIA = ? ORDER BY FECHA ASC";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idIncidencia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comentarios.add(mapRowToDTO(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findByIncidenciaId: " + e.getMessage());
        }
        return comentarios;
    }

    // ******************************************************
    // 2. OPERACIONES CRUD BÁSICAS
    // ******************************************************

    @Override
    public ComentarioDTO findById(Long id) {
        String sql = "SELECT * FROM COMENTARIO WHERE ID = ?";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDTO(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findById (Comentario): " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<ComentarioDTO> findAll() {
        List<ComentarioDTO> comentarios = new ArrayList<>();
        String sql = "SELECT * FROM COMENTARIO ORDER BY FECHA DESC";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                comentarios.add(mapRowToDTO(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en findAll (Comentario): " + e.getMessage());
        }
        return comentarios;
    }

    @Override
    public void save(ComentarioDTO comentario) {
        // ID es IDENTITY y FECHA tiene un valor por defecto (CURRENT_TIMESTAMP)
        String sql = "INSERT INTO COMENTARIO (ID_INCIDENCIA, DNI_AUTOR, TEXTO) VALUES (?, ?, ?)";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, comentario.getIncidencia().getId());
            ps.setString(2, comentario.getAutor().getDni());
            ps.setString(3, comentario.getTexto());
            
            ps.executeUpdate();
            
            // Recuperar el ID generado
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comentario.setId(generatedKeys.getLong(1));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al guardar comentario: " + e.getMessage());
        }
    }

    @Override
    public void update(ComentarioDTO comentario) {
        // Solo permitimos la actualización del texto del comentario
        String sql = "UPDATE COMENTARIO SET TEXTO = ? WHERE ID = ?";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, comentario.getTexto());
            ps.setLong(2, comentario.getId()); 

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al actualizar comentario: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM COMENTARIO WHERE ID = ?";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar comentario: " + e.getMessage());
        }
    }
}