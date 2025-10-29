package es.tew.dao;

import java.util.List;
import es.tew.dto.ComentarioDTO;

/**
 * Interfaz de Acceso a Datos (DAO) para la entidad Comentario.
 * Define las operaciones CRUD y las consultas específicas para la gestión 
 * de los comentarios asociados a las incidencias.
 */
public interface ComentarioDAO {

    // **********************************************
    // Consultas Específicas del Negocio
    // **********************************************
    
    /**
     * Recupera todos los comentarios asociados a una incidencia específica.
     * @param idIncidencia ID de la incidencia.
     * @return Lista de ComentarioDTO, ordenados por fecha ascendente.
     */
    List<ComentarioDTO> findByIncidenciaId(Long idIncidencia);

    // **********************************************
    // Operaciones CRUD Básicas
    // **********************************************

    /**
     * Busca un comentario por su ID.
     * @param id ID del comentario.
     * @return ComentarioDTO o null si no existe.
     */
    ComentarioDTO findById(Long id);

    /**
     * Obtiene una lista de todos los comentarios en el sistema.
     * @return Lista de ComentarioDTO.
     */
    List<ComentarioDTO> findAll();

    /**
     * Guarda un nuevo comentario. El ID debe ser generado por la base de datos.
     * @param comentario ComentarioDTO a guardar.
     */
    void save(ComentarioDTO comentario);

    /**
     * Actualiza un comentario existente (generalmente solo el texto).
     * @param comentario ComentarioDTO con los datos actualizados.
     */
    void update(ComentarioDTO comentario);

    /**
     * Elimina un comentario por su ID.
     * @param id ID del comentario a eliminar.
     */
    void delete(Long id);
}