package es.tew.logica;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.tew.dao.ComentarioDAO;
import es.tew.dao.DAOFactory;
import es.tew.dao.HistorialEstadoDAO;
import es.tew.dao.IncidenciaDAO;
import es.tew.dao.UsuarioDAO;
import es.tew.dto.ComentarioDTO;
import es.tew.dto.HistorialEstadoDTO;
import es.tew.dto.IncidenciaDTO;
import es.tew.dto.UsuarioDTO;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Servicio de lógica de negocio para la gestión de incidencias.
 * Implementa todas las reglas de negocio de la aplicación.
 */
@ApplicationScoped
public class ServicioIncidencias {

    private final IncidenciaDAO incidenciaDAO;
    private final UsuarioDAO usuarioDAO;
    private final ComentarioDAO comentarioDAO;
    private final HistorialEstadoDAO historialEstadoDAO;

    // Caracteres para generar contraseñas
    private static final String CARACTERES_PASSWORD = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LONGITUD_PASSWORD = 8;

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
    
    public List<UsuarioDTO> getAllUsuarios() {
        return usuarioDAO.findAll();
    }
    
    /**
     * Da de alta un nuevo usuario en el sistema.
     * Genera automáticamente una contraseña aleatoria.
     * 
     * @param usuario Usuario a dar de alta (sin contraseña)
     * @return La contraseña generada (para mostrarla al admin)
     */
    public String altaUsuario(UsuarioDTO usuario) {
        String passwordGenerada = generarPasswordAleatoria();
        usuario.setPassword(passwordGenerada);
        usuarioDAO.save(usuario);
        return passwordGenerada;
    }
    
    /**
     * Genera una contraseña aleatoria segura.
     */
    private String generarPasswordAleatoria() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(LONGITUD_PASSWORD);
        
        for (int i = 0; i < LONGITUD_PASSWORD; i++) {
            int index = random.nextInt(CARACTERES_PASSWORD.length());
            password.append(CARACTERES_PASSWORD.charAt(index));
        }
        
        return password.toString();
    }
    
    // --- LÓGICA DE INCIDENCIA ---

    /**
     * Crea una nueva incidencia asignándole automáticamente el técnico
     * con menos incidencias pendientes.
     */
    public IncidenciaDTO crearIncidencia(IncidenciaDTO incidencia) {
        incidencia.setEstado("ABIERTA");
        
        // FUNCIONALIDAD 3: Asignar automáticamente el técnico con menos incidencias
        UsuarioDTO tecnicoAsignado = obtenerTecnicoConMenosIncidencias();
        if (tecnicoAsignado != null) {
            incidencia.setTecnico(tecnicoAsignado);
        }
        
        // Guardar la incidencia y capturar el ID generado
        IncidenciaDTO incidenciaGuardada = incidenciaDAO.save(incidencia);
        
        // Registrar en el historial el estado inicial
        HistorialEstadoDTO historial = new HistorialEstadoDTO();
        historial.setIncidencia(incidenciaGuardada);
        historial.setEstadoAnterior(null); // Primera entrada, no hay estado anterior
        historial.setEstadoNuevo("ABIERTA");
        historial.setUsuario(incidenciaGuardada.getSolicitante());
        
        historialEstadoDAO.save(historial);
        
        return incidenciaGuardada;
    }
    
    /**
     * Encuentra el técnico con menos incidencias abiertas o en proceso.
     */
    private UsuarioDTO obtenerTecnicoConMenosIncidencias() {
        List<UsuarioDTO> tecnicos = getTecnicos();
        
        if (tecnicos.isEmpty()) {
            return null;
        }
        
        // Contar incidencias por técnico
        Map<String, Integer> conteoIncidencias = new HashMap<>();
        
        for (UsuarioDTO tecnico : tecnicos) {
            List<IncidenciaDTO> incidenciasTecnico = incidenciaDAO.findByTecnicoDni(tecnico.getDni());
            // Solo contar las que no están cerradas
            long incidenciasAbiertas = incidenciasTecnico.stream()
                .filter(inc -> !"CERRADA".equals(inc.getEstado()))
                .count();
            conteoIncidencias.put(tecnico.getDni(), (int) incidenciasAbiertas);
        }
        
        // Encontrar el técnico con menos incidencias
        String dniTecnicoMenorCarga = tecnicos.stream()
            .min((t1, t2) -> Integer.compare(
                conteoIncidencias.getOrDefault(t1.getDni(), 0),
                conteoIncidencias.getOrDefault(t2.getDni(), 0)
            ))
            .map(UsuarioDTO::getDni)
            .orElse(null);
        
        return dniTecnicoMenorCarga != null ? getUsuarioByDni(dniTecnicoMenorCarga) : null;
    }

    public void updateIncidencia(IncidenciaDTO incidenciaActualizada, UsuarioDTO usuarioModificador) {
        IncidenciaDTO incidenciaOriginal = incidenciaDAO.findById(incidenciaActualizada.getId());
        
        if (incidenciaOriginal == null) {
            throw new RuntimeException("Incidencia no encontrada para actualizar.");
        }
        
        // Si cambió el estado, registrarlo en el historial
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
     * FUNCIONALIDAD 9: Obtiene todas las incidencias con filtros opcionales.
     */
    public List<IncidenciaDTO> getIncidenciasConFiltros(String dniEmpleado, String estado) {
        List<IncidenciaDTO> incidencias = incidenciaDAO.findAll();
        
        // Aplicar filtro de empleado si se especificó
        if (dniEmpleado != null && !dniEmpleado.isEmpty()) {
            incidencias = incidencias.stream()
                .filter(inc -> inc.getSolicitante().getDni().equals(dniEmpleado))
                .collect(Collectors.toList());
        }
        
        // Aplicar filtro de estado si se especificó
        if (estado != null && !estado.isEmpty()) {
            incidencias = incidencias.stream()
                .filter(inc -> inc.getEstado().equals(estado))
                .collect(Collectors.toList());
        }
        
        return incidencias;
    }

    public IncidenciaDTO findIncidenciaById(Long id) {
        return incidenciaDAO.findById(id);
    }
    
    // --- LÓGICA DE COMENTARIOS Y HISTORIAL ---

    public void addComentario(ComentarioDTO comentario) {
        comentarioDAO.save(comentario);
    }

    public List<HistorialEstadoDTO> findHistorialByIncidenciaId(Long idIncidencia) {
        return historialEstadoDAO.findByIncidenciaId(idIncidencia);
    }
    
    public List<ComentarioDTO> findComentariosByIncidenciaId(Long idIncidencia) {
        return comentarioDAO.findByIncidenciaId(idIncidencia);
    }
    
    // --- LÓGICA DE ESTADÍSTICAS (FUNCIONALIDAD 12) ---
    
    /**
     * Obtiene el número de incidencias por estado.
     */
    public Map<String, Long> getIncidenciasPorEstado() {
        List<IncidenciaDTO> todasIncidencias = incidenciaDAO.findAll();
        
        Map<String, Long> estadisticas = new HashMap<>();
        estadisticas.put("ABIERTA", 0L);
        estadisticas.put("EN_PROCESO", 0L);
        estadisticas.put("PENDIENTE_USUARIO", 0L);
        estadisticas.put("CERRADA", 0L);
        
        for (IncidenciaDTO inc : todasIncidencias) {
            String estado = inc.getEstado();
            estadisticas.put(estado, estadisticas.getOrDefault(estado, 0L) + 1);
        }
        
        return estadisticas;
    }
    
    /**
     * Obtiene el número de incidencias por categoría.
     */
    public Map<String, Long> getIncidenciasPorCategoria() {
        List<IncidenciaDTO> todasIncidencias = incidenciaDAO.findAll();
        
        Map<String, Long> estadisticas = new HashMap<>();
        
        for (IncidenciaDTO inc : todasIncidencias) {
            String categoria = inc.getCategoria();
            estadisticas.put(categoria, estadisticas.getOrDefault(categoria, 0L) + 1);
        }
        
        return estadisticas;
    }
    
    /**
     * Calcula el tiempo medio de resolución de incidencias cerradas (en horas).
     */
    public double getTiempoMedioResolucion() {
        List<IncidenciaDTO> incidenciasCerradas = incidenciaDAO.findByEstado("CERRADA");
        
        if (incidenciasCerradas.isEmpty()) {
            return 0.0;
        }
        
        long sumaHoras = 0;
        int conteo = 0;
        
        for (IncidenciaDTO inc : incidenciasCerradas) {
            // Buscar en el historial cuándo se cerró
            List<HistorialEstadoDTO> historial = historialEstadoDAO.findByIncidenciaId(inc.getId());
            
            for (HistorialEstadoDTO cambio : historial) {
                if ("CERRADA".equals(cambio.getEstadoNuevo())) {
                    long diferenciaMilis = cambio.getFechaCambio().getTime() - inc.getFechaCreacion().getTime();
                    long horas = diferenciaMilis / (1000 * 60 * 60);
                    sumaHoras += horas;
                    conteo++;
                    break;
                }
            }
        }
        
        return conteo > 0 ? (double) sumaHoras / conteo : 0.0;
    }
    
    /**
     * Obtiene la incidencia abierta más antigua que no esté cerrada.
     */
    public IncidenciaDTO getIncidenciaMasAntiguaAbierta() {
        List<IncidenciaDTO> todasIncidencias = incidenciaDAO.findAll();
        
        return todasIncidencias.stream()
            .filter(inc -> !"CERRADA".equals(inc.getEstado()))
            .min((inc1, inc2) -> inc1.getFechaCreacion().compareTo(inc2.getFechaCreacion()))
            .orElse(null);
    }
}