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
        List<HistorialEstadoDTO> historiales = new ArrayList<>();
        String sql = "SELECT h.*, u.NOMBRE, u.APELLIDOS, u.ROL FROM HISTORIAL_ESTADO h LEFT JOIN USUARIO u ON h.DNI_USUARIO = u.DNI WHERE h.ID_INCIDENCIA = ? ORDER BY h.FECHA_CAMBIO DESC";

        // CORRECCIÓN: La conexión NO se incluye en el try-with-resources.
        try (PreparedStatement ps = DAOFactory.getConnection().prepareStatement(sql)) {
            ps.setLong(1, idIncidencia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialEstadoDTO historial = new HistorialEstadoDTO();
                    historial.setId(rs.getLong("ID"));
                    // Rellenar el resto de datos...
                    // (código para mapear el ResultSet a DTO)
                    historiales.add(historial);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar el historial: " + e.getMessage());
            e.printStackTrace();
        }
        return historiales;
    }

    // ******************************************************
    // 2. OPERACIÓN DE ESCRITURA
    // ******************************************************

    @Override
    public void save(HistorialEstadoDTO historial) {
        String sql = "INSERT INTO HISTORIAL_ESTADO (ID_INCIDENCIA, FECHA_CAMBIO, ESTADO_ANTERIOR, ESTADO_NUEVO, DNI_USUARIO) VALUES (?, ?, ?, ?, ?)";

        // CORRECCIÓN: La conexión NO se incluye en el try-with-resources para evitar que se cierre.
        try (PreparedStatement ps = DAOFactory.getConnection().prepareStatement(sql)) {
            ps.setLong(1, historial.getIncidencia().getId());
            ps.setTimestamp(2, new java.sql.Timestamp(historial.getFechaCambio().getTime()));
            ps.setString(3, historial.getEstadoAnterior());
            ps.setString(4, historial.getEstadoNuevo());
            if (historial.getUsuario() != null) {
                ps.setString(5, historial.getUsuario().getDni());
            } else {
                ps.setNull(5, java.sql.Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar el historial: " + e.getMessage());
            e.printStackTrace();
        }
    }
}