package es.tew.logica;

import es.tew.dao.DAOFactory;
import es.tew.dao.IncidenciaDAO;
import es.tew.dao.UsuarioDAO;
import es.tew.dao.ComentarioDAO;
import es.tew.dao.HistorialEstadoDAO;
import es.tew.dto.ComentarioDTO;
import es.tew.dto.HistorialEstadoDTO;
import es.tew.dto.IncidenciaDTO;
import es.tew.dto.UsuarioDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Clase que implementa la Lógica de Negocio de la aplicación.
 * Actúa como fachada para los DAOs e implementa las reglas de negocio.
 * En una aplicación real, esta clase se gestionaría con Inyección de Dependencias.
 */
@ApplicationScoped
public class ServicioIncidencias {

    // Instancias de los DAOs que se usarán en esta capa
    private final IncidenciaDAO incidenciaDAO;
    private final UsuarioDAO usuarioDAO;
    private final ComentarioDAO comentarioDAO;
    private final HistorialEstadoDAO historialEstadoDAO;

    /**
     * Constructor. Inicializa los DAOs a través de la factoría.
     */
    public ServicioIncidencias() {
        DAOFactory factory = DAOFactory.getFactory();
        this.incidenciaDAO = factory.getIncidenciaDAO();
        this.usuarioDAO = factory.getUsuarioDAO();
        this.comentarioDAO = factory.getComentarioDAO();
        this.historialEstadoDAO = factory.getHistorialEstadoDAO();
    }

    // ******************************************************
    // 1. LÓGICA DE USUARIO (LOGIN Y ROLES)
    // ******************************************************

    /**
     * Intenta autenticar a un usuario y lo devuelve si las credenciales son correctas.
     * @param dni DNI del usuario.
     * @param password Contraseña del usuario.
     * @return UsuarioDTO si es válido, null en caso contrario.
     */
    public UsuarioDTO login(String dni, String password) {
        return usuarioDAO.findByDniAndPassword(dni, password);
    }
    
    // NOTA: Se podrían añadir aquí métodos para registrar o actualizar usuarios
    
    public List<UsuarioDTO> getTecnicos() {
        // En una aplicación real, se necesitaría un método en UsuarioDAO para filtrar por ROL.
        // Por ahora, traemos todos y filtramos en la capa de lógica (ineficiente, pero funcional).
        List<UsuarioDTO> todos = usuarioDAO.findAll();
        todos.removeIf(u -> !u.getRol().equals("TECNICO"));
        return todos;
    }

    // ******************************************************
    // 2. LÓGICA DE INCIDENCIA
    // ******************************************************

    /**
     * Crea una nueva incidencia y registra su estado inicial en el historial.
     * @param incidencia La nueva incidencia a crear (debe tener el Solicitante y el Título/Descripción).
     * @return La IncidenciaDTO guardada con su ID.
     */
    public IncidenciaDTO crearIncidencia(IncidenciaDTO incidencia) {
        // Regla de Negocio 1: Toda nueva incidencia empieza como "Abierta"
        incidencia.setEstado("Abierta");
        
        // 1. Guardar la incidencia en la BDD
        incidenciaDAO.save(incidencia);
        
        // 2. Registrar el estado inicial en el historial
        HistorialEstadoDTO historial = new HistorialEstadoDTO();
        historial.setIncidencia(incidencia);
        historial.setEstadoNuevo("Abierta");
        historial.setUsuario(incidencia.getSolicitante()); // El solicitante es quien la crea
        
        historialEstadoDAO.save(historial);
        
        return incidencia;
    }

    /**
     * Actualiza una incidencia y registra el cambio de estado si ocurre.
     * @param incidenciaActualizada La incidencia con los nuevos datos.
     * @param usuarioModificador El usuario que realiza la modificación (para el historial).
     */
    public void actualizarIncidencia(IncidenciaDTO incidenciaActualizada, UsuarioDTO usuarioModificador) {
        // Obtener la incidencia actual desde la BDD para comparar el estado
        IncidenciaDTO incidenciaOriginal = incidenciaDAO.findById(incidenciaActualizada.getId());
        
        if (incidenciaOriginal == null) {
            // Manejar error: la incidencia no existe
            throw new RuntimeException("Incidencia no encontrada para actualizar.");
        }
        
        // 1. Registrar cambio de estado si es diferente
        if (!incidenciaOriginal.getEstado().equals(incidenciaActualizada.getEstado())) {
            
            // Regla de Negocio 2: Si el estado cambia, registrar en el historial
            HistorialEstadoDTO historial = new HistorialEstadoDTO();
            historial.setIncidencia(incidenciaActualizada);
            historial.setEstadoAnterior(incidenciaOriginal.getEstado());
            historial.setEstadoNuevo(incidenciaActualizada.getEstado());
            historial.setUsuario(usuarioModificador);
            
            historialEstadoDAO.save(historial);
        }
        
        // 2. Actualizar la incidencia en la BDD
        incidenciaDAO.update(incidenciaActualizada);
    }
    
    // ******************************************************
    // 3. CONSULTAS DE INCIDENCIAS POR ROL
    // ******************************************************
    
    /**
     * Obtiene las incidencias relevantes para un usuario según su rol.
     */
    public List<IncidenciaDTO> getIncidenciasByRol(UsuarioDTO usuario) {
        if (usuario == null) return null;

        switch (usuario.getRol()) {
            case "ADMIN":
                // El administrador ve todas
                return incidenciaDAO.findAll();
            case "TECNICO":
                // El técnico ve las que tiene asignadas
                return incidenciaDAO.findByTecnicoDni(usuario.getDni());
            case "USUARIO":
                // El usuario ve las que ha solicitado
                return incidenciaDAO.findBySolicitanteDni(usuario.getDni());
            default:
                return null;
        }
    }

    /**
     * Obtiene una incidencia específica por ID.
     */
    public IncidenciaDTO getIncidenciaById(Long id) {
        return incidenciaDAO.findById(id);
    }
    
    // ******************************************************
    // 4. LÓGICA DE COMENTARIOS Y HISTORIAL
    // ******************************************************

    /**
     * Añade un comentario a una incidencia.
     */
    public void addComentario(ComentarioDTO comentario) {
        comentarioDAO.save(comentario);
    }

    /**
     * Obtiene el historial de una incidencia.
     */
    public List<HistorialEstadoDTO> getHistorialIncidencia(Long idIncidencia) {
        return historialEstadoDAO.findByIncidenciaId(idIncidencia);
    }
    
    /**
     * Obtiene los comentarios de una incidencia.
     */
    public List<ComentarioDTO> getComentariosIncidencia(Long idIncidencia) {
        return comentarioDAO.findByIncidenciaId(idIncidencia);
    }
    public UsuarioDTO getUsuarioByDni(String dni) {
        // Reutiliza la función del DAO
        return usuarioDAO.findByDni(dni);
    }

}