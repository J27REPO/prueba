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
        String sql = "SELECT c.*, u.NOMBRE, u.APELLIDOS, u.ROL FROM COMENTARIO c JOIN USUARIO u ON c.DNI_AUTOR = u.DNI WHERE c.ID_INCIDENCIA = ? ORDER BY c.FECHA ASC";

        // CORRECCIÓN: La conexión NO se incluye en el try-with-resources.
        try (PreparedStatement ps = DAOFactory.getConnection().prepareStatement(sql)) {
            ps.setLong(1, idIncidencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ComentarioDTO comentario = new ComentarioDTO();
                    // ... (código para mapear el ResultSet a DTO)
                    comentarios.add(comentario);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar comentarios: " + e.getMessage());
            e.printStackTrace();
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
        String sql = "INSERT INTO COMENTARIO (TEXTO, FECHA, ID_INCIDENCIA, DNI_AUTOR) VALUES (?, ?, ?, ?)";
        
        // CORRECCIÓN: La conexión NO se incluye en el try-with-resources.
        try (PreparedStatement ps = DAOFactory.getConnection().prepareStatement(sql)) {
            ps.setString(1, comentario.getTexto());
            ps.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            ps.setLong(3, comentario.getIncidencia().getId());
            ps.setString(4, comentario.getAutor().getDni());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar comentario: " + e.getMessage());
            e.printStackTrace();
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