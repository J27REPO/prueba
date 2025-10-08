package es.tew.web;

import es.tew.dto.ComentarioDTO;
import es.tew.dto.HistorialEstadoDTO;
import es.tew.dto.IncidenciaDTO;
import es.tew.dto.UsuarioDTO;
import es.tew.logica.ServicioIncidencias;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped; // Requiere dependencia de JSF/CDI más reciente (Jakarta EE)
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Managed Bean encargado de la vista de detalle de una incidencia específica.
 * Gestiona la carga de la incidencia, comentarios, historial, y las acciones de técnico.
 */
@Named("detalleController")
@ViewScoped
public class DetalleController implements Serializable {

    private static final long serialVersionUID = 1L;

    // Inyección de Componentes
    @Inject
    private SesionController sesionController;
    @Inject
    private ServicioIncidencias servicioIncidencias;

    // Propiedades de la Incidencia (ID cargado por parámetro de vista)
    private Long idIncidencia;
    private IncidenciaDTO incidencia;

    // Listas de datos relacionados
    private List<ComentarioDTO> comentarios;
    private List<HistorialEstadoDTO> historial;
    private List<UsuarioDTO> tecnicosDisponibles;

    // Propiedades para formularios
    private ComentarioDTO nuevoComentario;
    
    // Propiedades para el dropdown de Estados
    private List<String> estadosDisponibles = Arrays.asList("Abierta", "Asignada", "En Proceso", "Cerrada");


    /**
     * Método de inicialización llamado automáticamente después de la inyección de dependencias.
     * Se usa para cargar los datos si el ID está presente.
     */
    @PostConstruct
    public void init() {
        // Inicializar el objeto para nuevo comentario
        nuevoComentario = new ComentarioDTO();
        
        // Cargar los técnicos disponibles para el dropdown de reasignación
        tecnicosDisponibles = servicioIncidencias.getTecnicos();
        
        // La carga de la incidencia y sus datos se delega a un método de carga 
        // que debería ser llamado por la vista (ej. con <f:viewParam> en JSF) o por init.
        // Lo simplificaremos asumiendo que el setter de idIncidencia lo dispara.
    }
    
    // **********************************************
    // 1. Métodos de Carga (Invocados por la Vista o el Setter)
    // **********************************************

    /**
     * Setter usado por <f:viewParam> en la página XHTML para pasar el ID.
     */
    public void setIdIncidencia(Long idIncidencia) {
        this.idIncidencia = idIncidencia;
        cargarIncidencia();
        cargarComentariosYHistorial();
    }
    
    /**
     * Carga la incidencia y las listas relacionadas.
     */
    public void cargarIncidencia() {
        if (idIncidencia != null) {
            this.incidencia = servicioIncidencias.getIncidenciaById(idIncidencia);
        }
    }
    
    private void cargarComentariosYHistorial() {
        if (idIncidencia != null) {
            this.comentarios = servicioIncidencias.getComentariosIncidencia(idIncidencia);
            this.historial = servicioIncidencias.getHistorialIncidencia(idIncidencia);
        }
    }

    // **********************************************
    // 2. Lógica de Comentarios
    // **********************************************

    /**
     * Acción para añadir un nuevo comentario.
     */
    public void addComentario() {
        if (nuevoComentario.getTexto() == null || nuevoComentario.getTexto().trim().isEmpty()) {
            return; // No hacer nada si el comentario está vacío
        }
        
        // Vincular el comentario a la incidencia y al autor actual
        nuevoComentario.setIncidencia(this.incidencia);
        nuevoComentario.setAutor(sesionController.getUsuarioActual());
        
        // Guardar en la capa de lógica
        servicioIncidencias.addComentario(nuevoComentario);
        
        // Recargar listas y limpiar el formulario
        cargarComentariosYHistorial();
        nuevoComentario = new ComentarioDTO(); // Limpiar para el siguiente comentario
    }

    // **********************************************
    // 3. Lógica de Modificación (Técnicos/Admin)
    // **********************************************

    /**
     * Acción para guardar los cambios de estado o reasignación.
     */
    public String guardarCambios() {
        if (incidencia == null) return null;

        // La incidencia ya tiene los valores actualizados del formulario (estado, técnico)
        
        // La lógica de negocio se encargará de:
        // 1. Comparar el estado actual y el anterior.
        // 2. Insertar el registro en HISTORIAL_ESTADO si el estado cambió.
        // 3. Actualizar la tabla INCIDENCIA.
        servicioIncidencias.actualizarIncidencia(this.incidencia, sesionController.getUsuarioActual());
        
        // Recargar listas para mostrar el historial y los nuevos datos
        cargarComentariosYHistorial();
        
        // Permanecer en la vista de detalle
        return null; 
    }

    // **********************************************
    // 4. Getters y Setters
    // **********************************************

    public Long getIdIncidencia() {
        return idIncidencia;
    }
    
    // (El setter setIdIncidencia() está arriba con la lógica de carga)

    public IncidenciaDTO getIncidencia() {
        return incidencia;
    }

    public void setIncidencia(IncidenciaDTO incidencia) {
        this.incidencia = incidencia;
    }

    public List<ComentarioDTO> getComentarios() {
        return comentarios;
    }

    public List<HistorialEstadoDTO> getHistorial() {
        return historial;
    }

    public ComentarioDTO getNuevoComentario() {
        return nuevoComentario;
    }

    public void setNuevoComentario(ComentarioDTO nuevoComentario) {
        this.nuevoComentario = nuevoComentario;
    }

    public List<UsuarioDTO> getTecnicosDisponibles() {
        return tecnicosDisponibles;
    }

    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }
}