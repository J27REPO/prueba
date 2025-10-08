package es.tew.dao;

import java.util.List;
import es.tew.dto.UsuarioDTO;

/**
 * Interfaz de Acceso a Datos (DAO) para la entidad Usuario.
 * Define las operaciones CRUD y de negocio para los usuarios.
 */
public interface UsuarioDAO {

    // Operación de negocio principal
    UsuarioDTO findByDniAndPassword(String dni, String password);
    
    // Operaciones CRUD básicas
    UsuarioDTO findByDni(String dni);
    List<UsuarioDTO> findAll();
    void save(UsuarioDTO usuario);
    void update(UsuarioDTO usuario);
    void delete(String dni);
    
}