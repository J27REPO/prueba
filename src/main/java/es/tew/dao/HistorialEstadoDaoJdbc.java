package es.tew.dao;

import es.tew.dto.HistorialEstadoDTO;
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
 * Implementación de HistorialEstadoDAO utilizando JDBC.
 * Solo implementa la lectura (findByIncidenciaId) y la escritura (save).
 */
public class HistorialEstadoDaoJdbc implements HistorialEstadoDAO {

    private final DAOFactory factory = DAOFactory.getFactory();
    private final IncidenciaDAO incidenciaDAO = factory.getIncidenciaDAO();
    private final UsuarioDAO usuarioDAO = factory.getUsuarioDAO();

    // Nombres de columnas de la tabla HISTORIAL_ESTADO
    private static final String ID = "ID";
    private static final String ID_INCIDENCIA = "ID_INCIDENCIA";
    private static final String FECHA_CAMBIO = "FECHA_CAMBIO";
    private static final String ESTADO_ANTERIOR = "ESTADO_ANTERIOR";
    private static final String ESTADO_NUEVO = "ESTADO_NUEVO";
    private static final String DNI_USUARIO = "DNI_USUARIO";

    // ******************************************************
    // UTILERÍA: Método para mapear un ResultSet a un HistorialEstadoDTO
    // ******************************************************
    
    /**
     * Convierte una fila de la tabla HISTORIAL_ESTADO en un objeto HistorialEstadoDTO,
     * resolviendo las relaciones con IncidenciaDTO y UsuarioDTO.
     * @param rs El ResultSet posicionado en una fila.
     * @return Un HistorialEstadoDTO completo.
     * @throws SQLException Si ocurre un error al acceder a la columna.
     */
    private HistorialEstadoDTO mapRowToDTO(ResultSet rs) throws SQLException {
        HistorialEstadoDTO hist = new HistorialEstadoDTO();
        hist.setId(rs.getLong(ID));
        hist.setFechaCambio(rs.getTimestamp(FECHA_CAMBIO));
        hist.setEstadoAnterior(rs.getString(ESTADO_ANTERIOR));
        hist.setEstadoNuevo(rs.getString(ESTADO_NUEVO));

        // 1. Resolver relación con Incidencia
        Long idIncidencia = rs.getLong(ID_INCIDENCIA);
        if (idIncidencia != 0) {
            // Reutilizamos el IncidenciaDAO para obtener solo el DTO de la incidencia (sin comentarios, etc.)
            IncidenciaDTO incidencia = incidenciaDAO.findById(idIncidencia);
            hist.setIncidencia(incidencia);
        }

        // 2. Resolver relación con Usuario (puede ser NULL si lo hace el sistema, aunque aquí siempre lo asociamos)
        String dniUsuario = rs.getString(DNI_USUARIO);
        if (dniUsuario != null) {
            // Reutilizamos el UsuarioDAO
            UsuarioDTO usuario = usuarioDAO.findByDni(dniUsuario);
            hist.setUsuario(usuario);
        }

        return hist;
    }

    // ******************************************************
    // 1. CONSULTA ESPECÍFICA DEL NEGOCIO
    // ******************************************************

    @Override
    public List<HistorialEstadoDTO> findByIncidenciaId(Long idIncidencia) {
        List<HistorialEstadoDTO> historial = new ArrayList<>();
        // Ordenamos por fecha ascendente para ver la evolución del estado
        String sql = "SELECT * FROM HISTORIAL_ESTADO WHERE ID_INCIDENCIA = ? ORDER BY FECHA_CAMBIO ASC";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, idIncidencia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    historial.add(mapRowToDTO(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findByIncidenciaId (Historial): " + e.getMessage());
        }
        return historial;
    }

    // ******************************************************
    // 2. OPERACIÓN DE ESCRITURA
    // ******************************************************

    @Override
    public void save(HistorialEstadoDTO historial) {
        // ID es IDENTITY y FECHA_CAMBIO tiene un valor por defecto (CURRENT_TIMESTAMP)
        String sql = "INSERT INTO HISTORIAL_ESTADO (ID_INCIDENCIA, ESTADO_ANTERIOR, ESTADO_NUEVO, DNI_USUARIO) VALUES (?, ?, ?, ?)";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, historial.getIncidencia().getId());
            
            // ESTADO_ANTERIOR puede ser NULL
            if (historial.getEstadoAnterior() != null) {
                ps.setString(2, historial.getEstadoAnterior());
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
            }
            
            ps.setString(3, historial.getEstadoNuevo());
            
            // DNI_USUARIO puede ser NULL o estar asociado al usuario que realiza el cambio
            if (historial.getUsuario() != null) {
                ps.setString(4, historial.getUsuario().getDni());
            } else {
                ps.setNull(4, java.sql.Types.VARCHAR);
            }
            
            ps.executeUpdate();
            
            // Recuperar el ID generado
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    historial.setId(generatedKeys.getLong(1));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error al guardar historial de estado: " + e.getMessage());
        }
    }
}