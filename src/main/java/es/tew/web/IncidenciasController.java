package es.tew.web;

import es.tew.dto.IncidenciaDTO;
import es.tew.dto.UsuarioDTO;
import es.tew.logica.ServicioIncidencias;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped; 
import java.io.Serializable;
import java.util.List;

@Named("incidenciasController")
@ViewScoped
public class IncidenciasController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private SesionController sesionController;

    @Inject
    private ServicioIncidencias servicioIncidencias;

    private List<IncidenciaDTO> listaIncidencias;
    private IncidenciaDTO nuevaIncidencia; 

    public List<IncidenciaDTO> getListaIncidencias() {
        if (listaIncidencias == null) {
            UsuarioDTO usuario = sesionController.getUsuarioActual();
            if (usuario != null) {
                listaIncidencias = servicioIncidencias.getIncidenciasByRol(usuario);
            }
        }
        return listaIncidencias;
    }
    
    public String initCreacion() {
        nuevaIncidencia = new IncidenciaDTO();
        return "crearIncidencia?faces-redirect=true"; 
    }
    
    public String guardarIncidencia() {
        // Asignamos el usuario actual como solicitante antes de guardar
        System.out.println("Guardando incidencia para el usuario: " + sesionController.getUsuarioActual().getNombre());
        nuevaIncidencia.setSolicitante(sesionController.getUsuarioActual());
        servicioIncidencias.crearIncidencia(nuevaIncidencia);
        
        // Forzamos la recarga de la lista en la siguiente vista
        listaIncidencias = null; 
        
        // Redirigimos directamente al listado
        return "/listado.xhtml?faces-redirect=true";
    }

    // --- GETTERS Y SETTERS ---
    
    public IncidenciaDTO getNuevaIncidencia() {
        if (nuevaIncidencia == null) {
            nuevaIncidencia = new IncidenciaDTO();
        }
        return nuevaIncidencia;
    }

    public void setNuevaIncidencia(IncidenciaDTO nuevaIncidencia) {
        this.nuevaIncidencia = nuevaIncidencia;
    }
}