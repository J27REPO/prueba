package es.tew.dao;

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
 * Implementación de IncidenciaDAO utilizando JDBC.
 */
public class IncidenciaDaoJdbc implements IncidenciaDAO {

    private final DAOFactory factory = DAOFactory.getFactory();
    private final UsuarioDAO usuarioDAO = factory.getUsuarioDAO();

    // Nombres de columnas de la tabla INCIDENCIA
    private static final String ID = "ID";
    private static final String TITULO = "TITULO";
    private static final String DESCRIPCION = "DESCRIPCION";
    private static final String FECHA_CREACION = "FECHA_CREACION";
    private static final String ESTADO = "ESTADO";
    private static final String CATEGORIA = "CATEGORIA";
    private static final String DNI_SOLICITANTE = "DNI_SOLICITANTE";
    private static final String DNI_TECNICO = "DNI_TECNICO";

    // ******************************************************
    // UTILERÍA: Método para mapear un ResultSet a un IncidenciaDTO
    // ******************************************************

    /**
     * Convierte una fila de la tabla INCIDENCIA en un objeto IncidenciaDTO,
     * resolviendo las relaciones con UsuarioDTO.
     * @param rs El ResultSet posicionado en una fila.
     * @return Un IncidenciaDTO completo.
     * @throws SQLException Si ocurre un error al acceder a la columna.
     */
    private IncidenciaDTO mapRowToDTO(ResultSet rs) throws SQLException {
        IncidenciaDTO inc = new IncidenciaDTO();
        inc.setId(rs.getLong(ID));
        inc.setTitulo(rs.getString(TITULO));
        inc.setDescripcion(rs.getString(DESCRIPCION));
        inc.setFechaCreacion(rs.getTimestamp(FECHA_CREACION));
        inc.setEstado(rs.getString(ESTADO));
        inc.setCategoria(rs.getString(CATEGORIA));

        // 1. Resolver relación con Solicitante
        String dniSolicitante = rs.getString(DNI_SOLICITANTE);
        if (dniSolicitante != null) {
            // Reutilizamos el UsuarioDAO para obtener el DTO completo del solicitante
            UsuarioDTO solicitante = usuarioDAO.findByDni(dniSolicitante);
            inc.setSolicitante(solicitante);
        }

        // 2. Resolver relación con Técnico (puede ser NULL)
        String dniTecnico = rs.getString(DNI_TECNICO);
        if (dniTecnico != null) {
            // Reutilizamos el UsuarioDAO para obtener el DTO completo del técnico
            UsuarioDTO tecnico = usuarioDAO.findByDni(dniTecnico);
            inc.setTecnico(tecnico);
        }

        return inc;
    }

    // ******************************************************
    // 1. OPERACIONES CRUD BÁSICAS
    // ******************************************************

    @Override
    public IncidenciaDTO findById(Long id) {
        String sql = "SELECT * FROM INCIDENCIA WHERE ID = ?";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDTO(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<IncidenciaDTO> findAll() {
        List<IncidenciaDTO> incidencias = new ArrayList<>();
        String sql = "SELECT * FROM INCIDENCIA ORDER BY FECHA_CREACION DESC";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                incidencias.add(mapRowToDTO(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en findAll: " + e.getMessage());
        }
        return incidencias;
    }

    @Override
    public void save(IncidenciaDTO incidencia) {
        // En HSQLDB, FECHA_CREACION tiene un valor por defecto (CURRENT_TIMESTAMP),
        // y el ID es generado automáticamente (IDENTITY).
        String sql = "INSERT INTO INCIDENCIA (TITULO, DESCRIPCION, ESTADO, CATEGORIA, DNI_SOLICITANTE, DNI_TECNICO) VALUES (?, ?, ?, ?, ?, ?)";

        // Usamos RETURN_GENERATED_KEYS para obtener el ID auto-generado
        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, incidencia.getTitulo());
            ps.setString(2, incidencia.getDescripcion());
            ps.setString(3, incidencia.getEstado());
            ps.setString(4, incidencia.getCategoria());
            ps.setString(5, incidencia.getSolicitante().getDni());
            
            // DNI_TECNICO puede ser NULL
            if (incidencia.getTecnico() != null) {
                ps.setString(6, incidencia.getTecnico().getDni());
            } else {
                ps.setNull(6, java.sql.Types.VARCHAR);
            }

            ps.executeUpdate();
            
            // Recuperar el ID generado
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    incidencia.setId(generatedKeys.getLong(1));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al guardar incidencia: " + e.getMessage());
        }
    }

    @Override
    public void update(IncidenciaDTO incidencia) {
        // Se puede actualizar todo menos ID, Fecha de Creación y Solicitante.
        String sql = "UPDATE INCIDENCIA SET TITULO = ?, DESCRIPCION = ?, ESTADO = ?, CATEGORIA = ?, DNI_TECNICO = ? WHERE ID = ?";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, incidencia.getTitulo());
            ps.setString(2, incidencia.getDescripcion());
            ps.setString(3, incidencia.getEstado());
            ps.setString(4, incidencia.getCategoria());
            
            // DNI_TECNICO puede ser NULL
            if (incidencia.getTecnico() != null) {
                ps.setString(5, incidencia.getTecnico().getDni());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }
            
            ps.setLong(6, incidencia.getId()); // Condición WHERE

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al actualizar incidencia: " + e.getMessage());
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM INCIDENCIA WHERE ID = ?";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar incidencia: " + e.getMessage());
            // NOTA: Una incidencia no podrá eliminarse si tiene COMENTARIOS o HISTORIAL asociados
            // debido a las claves foráneas. Deberías eliminar esos registros primero.
        }
    }

    // ******************************************************
    // 2. CONSULTAS ESPECÍFICAS DEL NEGOCIO
    // ******************************************************

    @Override
    public List<IncidenciaDTO> findBySolicitanteDni(String dniSolicitante) {
        List<IncidenciaDTO> incidencias = new ArrayList<>();
        String sql = "SELECT * FROM INCIDENCIA WHERE DNI_SOLICITANTE = ? ORDER BY FECHA_CREACION DESC";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dniSolicitante);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    incidencias.add(mapRowToDTO(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findBySolicitanteDni: " + e.getMessage());
        }
        return incidencias;
    }

    @Override
    public List<IncidenciaDTO> findByTecnicoDni(String dniTecnico) {
        List<IncidenciaDTO> incidencias = new ArrayList<>();
        // Un técnico puede ver las que tiene asignadas y las que ya cerró
        String sql = "SELECT * FROM INCIDENCIA WHERE DNI_TECNICO = ? ORDER BY FECHA_CREACION DESC";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dniTecnico);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    incidencias.add(mapRowToDTO(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findByTecnicoDni: " + e.getMessage());
        }
        return incidencias;
    }

    @Override
    public List<IncidenciaDTO> findByEstado(String estado) {
        List<IncidenciaDTO> incidencias = new ArrayList<>();
        String sql = "SELECT * FROM INCIDENCIA WHERE ESTADO = ? ORDER BY FECHA_CREACION DESC";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estado);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    incidencias.add(mapRowToDTO(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findByEstado: " + e.getMessage());
        }
        return incidencias;
    }
}