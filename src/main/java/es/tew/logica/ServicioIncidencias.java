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
 */
@ApplicationScoped
public class ServicioIncidencias {

    private final IncidenciaDAO incidenciaDAO;
    private final UsuarioDAO usuarioDAO;
    private final ComentarioDAO comentarioDAO;
    private final HistorialEstadoDAO historialEstadoDAO;

    public ServicioIncidencias() {
        DAOFactory factory = DAOFactory.getFactory();
        this.incidenciaDAO = factory.getIncidenciaDAO();
        this.usuarioDAO = factory.getUsuarioDAO();
        this.comentarioDAO = factory.getComentarioDAO();
        this.historialEstadoDAO = factory.getHistorialEstadoDAO();
    }

    // --- LÓGICA DE USUARIO ---

    public UsuarioDTO login(String dni, String password) {
        return usuarioDAO.findByDniAndPassword(dni, password);
    }
    
    public List<UsuarioDTO> getTecnicos() {
        List<UsuarioDTO> todos = usuarioDAO.findAll();
        todos.removeIf(u -> !"TECNICO".equals(u.getRol()));
        return todos;
    }

    public UsuarioDTO getUsuarioByDni(String dni) {
        return usuarioDAO.findByDni(dni);
    }
    
    // --- LÓGICA DE INCIDENCIA ---

    public IncidenciaDTO crearIncidencia(IncidenciaDTO incidencia) {
        incidencia.setEstado("ABIERTA");
        
        // CORRECCIÓN: Capturamos la incidencia devuelta con el ID ya asignado.
        IncidenciaDTO incidenciaGuardada = incidenciaDAO.save(incidencia);
        
        // Ahora usamos el objeto con el ID correcto para guardar el historial.
        HistorialEstadoDTO historial = new HistorialEstadoDTO();
        historial.setIncidencia(incidenciaGuardada);
        historial.setEstadoNuevo("ABIERTA");
        historial.setUsuario(incidenciaGuardada.getSolicitante());
        
        historialEstadoDAO.save(historial);
        
        return incidenciaGuardada;
    }

    /**
     * CORRECCIÓN: Renombrado de 'actualizarIncidencia' a 'updateIncidencia' para coincidir con el Controller.
     */
    public void updateIncidencia(IncidenciaDTO incidenciaActualizada, UsuarioDTO usuarioModificador) {
        IncidenciaDTO incidenciaOriginal = incidenciaDAO.findById(incidenciaActualizada.getId());
        
        if (incidenciaOriginal == null) {
            throw new RuntimeException("Incidencia no encontrada para actualizar.");
        }
        
        if (!incidenciaOriginal.getEstado().equals(incidenciaActualizada.getEstado())) {
            HistorialEstadoDTO historial = new HistorialEstadoDTO();
            historial.setIncidencia(incidenciaActualizada);
            historial.setEstadoAnterior(incidenciaOriginal.getEstado());
            historial.setEstadoNuevo(incidenciaActualizada.getEstado());
            historial.setUsuario(usuarioModificador);
            historialEstadoDAO.save(historial);
        }
        
        incidenciaDAO.update(incidenciaActualizada);
    }
    
    public List<IncidenciaDTO> getIncidenciasByRol(UsuarioDTO usuario) {
        if (usuario == null) return null;

        switch (usuario.getRol()) {
            case "ADMIN":
                return incidenciaDAO.findAll();
            case "TECNICO":
                return incidenciaDAO.findByTecnicoDni(usuario.getDni());
            case "USUARIO":
                return incidenciaDAO.findBySolicitanteDni(usuario.getDni());
            default:
                return null;
        }
    }

    /**
     * CORRECCIÓN: Renombrado de 'getIncidenciaById' a 'findIncidenciaById' para coincidir con el Controller.
     */
    public IncidenciaDTO findIncidenciaById(Long id) {
        return incidenciaDAO.findById(id);
    }
    
    // --- LÓGICA DE COMENTARIOS Y HISTORIAL ---

    public void addComentario(ComentarioDTO comentario) {
        comentarioDAO.save(comentario);
    }

    /**
     * CORRECCIÓN: Renombrado de 'getHistorialIncidencia' a 'findHistorialByIncidenciaId' para coincidir con el Controller.
     */
    public List<HistorialEstadoDTO> findHistorialByIncidenciaId(Long idIncidencia) {
        return historialEstadoDAO.findByIncidenciaId(idIncidencia);
    }
    
    /**
     * CORRECCIÓN: Renombrado de 'getComentariosIncidencia' a 'findComentariosByIncidenciaId' para coincidir con el Controller.
     */
    public List<ComentarioDTO> findComentariosByIncidenciaId(Long idIncidencia) {
        return comentarioDAO.findByIncidenciaId(idIncidencia);
    }
}