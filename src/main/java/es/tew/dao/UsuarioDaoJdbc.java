package es.tew.dao;

import es.tew.dto.UsuarioDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de UsuarioDAO utilizando JDBC.
 */
public class UsuarioDaoJdbc implements UsuarioDAO {

    // Nombres de columnas para evitar errores de tipeo
    private static final String DNI = "DNI";
    private static final String NOMBRE = "NOMBRE";
    private static final String APELLIDOS = "APELLIDOS";
    private static final String PASSWD = "PASSWD";
    private static final String ROL = "ROL";

    // ******************************************************
    // UTILERÍA: Método para mapear un ResultSet a un UsuarioDTO
    // ******************************************************
    
    /**
     * Convierte una fila de la tabla USUARIO (representada por un ResultSet) en un objeto UsuarioDTO.
     * @param rs El ResultSet posicionado en una fila.
     * @return Un UsuarioDTO.
     * @throws SQLException Si ocurre un error al acceder a la columna.
     */
    private UsuarioDTO mapRowToDTO(ResultSet rs) throws SQLException {
        UsuarioDTO user = new UsuarioDTO();
        user.setDni(rs.getString(DNI));
        user.setNombre(rs.getString(NOMBRE));
        user.setApellidos(rs.getString(APELLIDOS));
        // NOTA: No se debe recuperar la contraseña para evitar exponerla fuera de la capa DAO
        // Sin embargo, para fines de LOGIN, se necesita solo en el método findByDniAndPassword.
        user.setRol(rs.getString(ROL));
        return user;
    }

    // ******************************************************
    // 1. OPERACIÓN DE NEGOCIO: LOGIN
    // ******************************************************

    /**
     * Busca un usuario por DNI y verifica la contraseña.
     */
    @Override
    public UsuarioDTO findByDniAndPassword(String dni, String password) {
        // En una aplicación real, la contraseña debe estar hasheada. 
        // Aquí la verificamos en texto plano según el script SQL.
        String sql = "SELECT * FROM USUARIO WHERE DNI = ? AND PASSWD = ?";
        
        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, dni);
            ps.setString(2, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Si encontramos un resultado, mapeamos la fila a un DTO.
                    // Nota: Usamos mapRowToDTO que ya omite la contraseña en el DTO final.
                    return mapRowToDTO(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findByDniAndPassword: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Retorna null si no se encuentra o la contraseña es incorrecta
    }

    // ******************************************************
    // 2. OPERACIONES CRUD BÁSICAS
    // ******************************************************

    /**
     * Busca un usuario por su DNI (clave primaria).
     */
    @Override
    public UsuarioDTO findByDni(String dni) {
        String sql = "SELECT * FROM USUARIO WHERE DNI = ?";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dni);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDTO(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findByDni: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene una lista de todos los usuarios.
     */
    @Override
    public List<UsuarioDTO> findAll() {
        List<UsuarioDTO> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM USUARIO";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapRowToDTO(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error en findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return usuarios;
    }

    /**
     * Inserta un nuevo usuario.
     */
    @Override
    public void save(UsuarioDTO usuario) {
        String sql = "INSERT INTO USUARIO (DNI, NOMBRE, APELLIDOS, PASSWD, ROL) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getDni());
            ps.setString(2, usuario.getNombre());
            ps.setString(3, usuario.getApellidos());
            ps.setString(4, usuario.getPassword()); // La contraseña debe venir ya del DTO
            ps.setString(5, usuario.getRol());
            
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al guardar usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Actualiza los datos de un usuario existente (excepto DNI).
     */
    @Override
    public void update(UsuarioDTO usuario) {
        // En este ejemplo, permitimos actualizar nombre, apellidos, password y rol.
        String sql = "UPDATE USUARIO SET NOMBRE = ?, APELLIDOS = ?, PASSWD = ?, ROL = ? WHERE DNI = ?";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getPassword()); 
            ps.setString(4, usuario.getRol());
            ps.setString(5, usuario.getDni()); // Condición WHERE
            
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Elimina un usuario por su DNI.
     */
    @Override
    public void delete(String dni) {
        String sql = "DELETE FROM USUARIO WHERE DNI = ?";

        try (Connection con = DAOFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dni);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }
}