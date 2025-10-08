package es.tew.web;

import es.tew.dto.IncidenciaDTO;
import es.tew.dto.UsuarioDTO;
import es.tew.logica.ServicioIncidencias;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped; // Podría ser ViewScoped si usas PrimeFaces
import java.io.Serializable;
import java.util.List;

/**
 * Managed Bean encargado de listar las incidencias relevantes para el usuario actual.
 * El scope se deja en RequestScoped por defecto, pero se recomienda ViewScoped 
 * en un entorno JSF/PrimeFaces para evitar recargas innecesarias de la lista.
 */
@Named("incidenciasController")
@RequestScoped 
// NOTA: Para PrimeFaces, es mejor usar @ViewScoped si la lista debe persistir 
// durante las peticiones AJAX en la misma vista.
public class IncidenciasController implements Serializable {

    private static final long serialVersionUID = 1L;

    // Inyección del Controlador de Sesión para acceder al usuario logueado
    @Inject
    private SesionController sesionController;

    // Inyección de la Capa de Lógica de Negocio
    @Inject
    private ServicioIncidencias servicioIncidencias;

    // Propiedades
    private List<IncidenciaDTO> listaIncidencias;
    private IncidenciaDTO incidenciaSeleccionada; // Para la selección en la tabla
    
    // Propiedad para la creación de una nueva incidencia
    private IncidenciaDTO nuevaIncidencia; 

    /**
     * Inicializa la lista de incidencias la primera vez que se accede.
     * Es crucial para que la lista se cargue al entrar en la vista.
     */
    public List<IncidenciaDTO> getListaIncidencias() {
        if (listaIncidencias == null) {
            UsuarioDTO usuario = sesionController.getUsuarioActual();
            if (usuario != null) {
                // La lógica de negocio filtra las incidencias según el rol
                listaIncidencias = servicioIncidencias.getIncidenciasByRol(usuario);
            }
        }
        return listaIncidencias;
    }
    
    /**
     * Método de acción para inicializar el objeto para crear una nueva incidencia.
     */
    public String initCreacion() {
        // Creamos un DTO vacío para el formulario
        nuevaIncidencia = new IncidenciaDTO();
        // Establecemos el solicitante automáticamente (es el usuario actual)
        nuevaIncidencia.setSolicitante(sesionController.getUsuarioActual());
        return "crearIncidencia"; // Navegación al formulario
    }
    
    /**
     * Método de acción para guardar la nueva incidencia.
     */
    public String guardarIncidencia() {
        // Validaciones mínimas (en la práctica se usarían Validadores JSF)
        if (nuevaIncidencia.getTitulo() == null || nuevaIncidencia.getTitulo().isEmpty()) {
            return null; // Quedarse en la página si falla la validación
        }
        
        // La lógica de negocio se encarga de asignar el estado "Abierta" 
        // y registrar el historial.
        servicioIncidencias.crearIncidencia(nuevaIncidencia);
        
        // Limpiamos la lista para forzar una recarga y ver la nueva incidencia
        listaIncidencias = null; 
        
        // Redirigir al listado principal
        return "exitoCreacion?faces-redirect=true"; 
    }

    /**
     * Método de acción para ver los detalles de una incidencia seleccionada.
     * @param idIncidencia ID de la incidencia seleccionada.
     * @return String de navegación a la vista de detalle.
     */
    public String verDetalle(Long idIncidencia) {
        // Cargamos la incidencia completa, si no está ya cargada
        incidenciaSeleccionada = servicioIncidencias.getIncidenciaById(idIncidencia);
        return "detalleIncidencia";
    }


    // **********************************************
    // Getters y Setters
    // **********************************************
    
    // NOTA: El getter getListaIncidencias() tiene lógica de inicialización, 
    // pero el resto son getters y setters estándar.

    public IncidenciaDTO getIncidenciaSeleccionada() {
        return incidenciaSeleccionada;
    }

    public void setIncidenciaSeleccionada(IncidenciaDTO incidenciaSeleccionada) {
        this.incidenciaSeleccionada = incidenciaSeleccionada;
    }

    public IncidenciaDTO getNuevaIncidencia() {
        return nuevaIncidencia;
    }

    public void setNuevaIncidencia(IncidenciaDTO nuevaIncidencia) {
        this.nuevaIncidencia = nuevaIncidencia;
    }
}