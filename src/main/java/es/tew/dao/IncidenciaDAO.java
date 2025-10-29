package es.tew.dao;

import java.util.List;

import es.tew.dto.IncidenciaDTO;
import es.tew.dto.UsuarioDTO;

/**
 * Interfaz de Acceso a Datos (DAO) para la entidad Incidencia.
 * Define las operaciones CRUD y las consultas específicas necesarias
 * para la gestión de incidencias en el sistema.
 */
public interface IncidenciaDAO {

    // **********************************************
    // Consultas Específicas del Negocio
    // **********************************************
    
    /**
     * Recupera todas las incidencias solicitadas por un usuario específico (ROL USUARIO).
     * @param dniSolicitante DNI del usuario solicitante.
     * @return Lista de IncidenciaDTO.
     */
    List<IncidenciaDTO> findBySolicitanteDni(String dniSolicitante);

    /**
     * Recupera todas las incidencias asignadas a un técnico específico (ROL TECNICO).
     * @param dniTecnico DNI del técnico asignado.
     * @return Lista de IncidenciaDTO.
     */
    List<IncidenciaDTO> findByTecnicoDni(String dniTecnico);
    
    /**
     * Recupera todas las incidencias en un estado específico (ej. "Abierta").
     * @param estado Estado de la incidencia.
     * @return Lista de IncidenciaDTO.
     */
    List<IncidenciaDTO> findByEstado(String estado);

    // **********************************************
    // Operaciones CRUD Básicas
    // **********************************************

    /**
     * Busca una incidencia por su ID.
     * @param id ID de la incidencia.
     * @return IncidenciaDTO o null si no existe.
     */
    IncidenciaDTO findById(Long id);

    /**
     * Obtiene una lista de todas las incidencias en el sistema (Generalmente para el ADMIN).
     * @return Lista de IncidenciaDTO.
     */
    List<IncidenciaDTO> findAll();

    /**
     * Guarda una nueva incidencia. El ID debe ser generado por la base de datos.
     * @param incidencia IncidenciaDTO a guardar.
     */
    IncidenciaDTO save(IncidenciaDTO incidencia);

    /**
     * Actualiza una incidencia existente.
     * @param incidencia IncidenciaDTO con los datos actualizados.
     */
    void update(IncidenciaDTO incidencia);

    /**
     * Elimina una incidencia por su ID.
     * @param id ID de la incidencia a eliminar.
     */
    void delete(Long id);
    
    List<UsuarioDTO> findByRol(String rol);
}