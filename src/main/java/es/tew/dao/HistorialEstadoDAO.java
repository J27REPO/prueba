package es.tew.dao;

import java.util.List;
import es.tew.dto.HistorialEstadoDTO;

/**
 * Interfaz de Acceso a Datos (DAO) para la entidad HistorialEstado.
 * Define las operaciones necesarias para registrar y consultar los cambios 
 * de estado de una incidencia. Esta entidad es de solo escritura y lectura.
 */
public interface HistorialEstadoDAO {

    // **********************************************
    // Consultas Específicas del Negocio
    // **********************************************
    
    /**
     * Recupera el historial de estados de una incidencia específica.
     * @param idIncidencia ID de la incidencia.
     * @return Lista de HistorialEstadoDTO, ordenados por fecha ascendente.
     */
    List<HistorialEstadoDTO> findByIncidenciaId(Long idIncidencia);

    // **********************************************
    // Operación de Escritura
    // **********************************************

    /**
     * Registra un nuevo cambio de estado en la tabla de historial.
     * @param historial HistorialEstadoDTO a guardar.
     */
    void save(HistorialEstadoDTO historial);

    // NOTA: No se incluyen métodos update, delete ni findById/findAll, 
    // ya que no se deben permitir la modificación ni la consulta masiva.
}