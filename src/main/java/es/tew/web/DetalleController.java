package es.tew.web;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import es.tew.dto.ComentarioDTO;
import es.tew.dto.HistorialEstadoDTO;
import es.tew.dto.IncidenciaDTO;
import es.tew.dto.UsuarioDTO;
import es.tew.logica.ServicioIncidencias;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("detalleController")
@ViewScoped
public class DetalleController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ServicioIncidencias servicioIncidencias;

    @Inject
    private SesionController sesionController;

    private Long idIncidencia;
    private IncidenciaDTO incidencia;
    private List<ComentarioDTO> comentarios;
    private List<HistorialEstadoDTO> historial;
    private ComentarioDTO nuevoComentario;
    private List<UsuarioDTO> tecnicosDisponibles;
    private final List<String> estadosDisponibles = Arrays.asList("EN_PROCESO", "PENDIENTE_USUARIO", "CERRADA");    
    @PostConstruct
    public void init() {
        nuevoComentario = new ComentarioDTO();
    }

    /**
     * PUNTO CLAVE 3: Este método se invoca desde la vista para cargar los datos.
     */
    public void cargarIncidencia() {
        if (idIncidencia != null) {
            this.incidencia = servicioIncidencias.findIncidenciaById(idIncidencia);
            if (this.incidencia != null) {
                this.comentarios = servicioIncidencias.findComentariosByIncidenciaId(idIncidencia);
                this.historial = servicioIncidencias.findHistorialByIncidenciaId(idIncidencia);
                this.tecnicosDisponibles = servicioIncidencias.getTecnicos();
            }
        }
    }

    public void addComentario() {
        if (nuevoComentario != null && nuevoComentario.getTexto() != null && !nuevoComentario.getTexto().trim().isEmpty()) {
            nuevoComentario.setAutor(sesionController.getUsuarioActual());
            nuevoComentario.setIncidencia(incidencia);
            servicioIncidencias.addComentario(nuevoComentario);
            this.comentarios = servicioIncidencias.findComentariosByIncidenciaId(idIncidencia);
            this.nuevoComentario = new ComentarioDTO();
        }
    }
    
    public void guardarCambios() {
        if (incidencia != null) {
            servicioIncidencias.updateIncidencia(incidencia, sesionController.getUsuarioActual());
            this.historial = servicioIncidencias.findHistorialByIncidenciaId(idIncidencia);
        }
    }
    
    // --- GETTERS Y SETTERS ---

    public Long getIdIncidencia() {
        return idIncidencia;
    }

    public void setIdIncidencia(Long idIncidencia) {
        this.idIncidencia = idIncidencia;
    }

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