package es.tew.web;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import es.tew.dto.IncidenciaDTO;
import es.tew.dto.UsuarioDTO;
import es.tew.logica.ServicioIncidencias;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Controlador para las funcionalidades exclusivas del administrador.
 */
@Named("adminController")
@ViewScoped
public class AdminController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ServicioIncidencias servicioIncidencias;

    @Inject
    private SesionController sesionController;

    // --- FUNCIONALIDAD 9: Listar todas las incidencias con filtros ---
    private List<IncidenciaDTO> todasIncidencias;
    private String filtroEmpleado;
    private String filtroEstado;
    private List<String> estadosDisponibles = Arrays.asList("", "ABIERTA", "EN_PROCESO", "PENDIENTE_USUARIO", "CERRADA");

    // --- FUNCIONALIDAD 10: Alta de usuarios ---
    private UsuarioDTO nuevoUsuario;
    private String passwordGenerada;
    private boolean mostrarPassword;
    private List<String> rolesDisponibles = Arrays.asList("USUARIO", "TECNICO");

    // --- FUNCIONALIDAD 11: Listar usuarios ---
    private List<UsuarioDTO> todosUsuarios;

    // --- FUNCIONALIDAD 12: Estadísticas ---
    private Map<String, Long> estadisticasEstado;
    private Map<String, Long> estadisticasCategoria;
    private double tiempoMedioResolucion;
    private IncidenciaDTO incidenciaMasAntigua;

    @PostConstruct
    public void init() {
        nuevoUsuario = new UsuarioDTO();
        mostrarPassword = false;
    }

    // ========================================
    // FUNCIONALIDAD 9: LISTAR TODAS LAS INCIDENCIAS CON FILTROS
    // ========================================

    public List<IncidenciaDTO> getTodasIncidencias() {
        if (todasIncidencias == null) {
            cargarIncidenciasConFiltros();
        }
        return todasIncidencias;
    }

    public void aplicarFiltros() {
        cargarIncidenciasConFiltros();
    }

    public void limpiarFiltros() {
        filtroEmpleado = null;
        filtroEstado = null;
        cargarIncidenciasConFiltros();
    }

    private void cargarIncidenciasConFiltros() {
        todasIncidencias = servicioIncidencias.getIncidenciasConFiltros(filtroEmpleado, filtroEstado);
    }

    // ========================================
    // FUNCIONALIDAD 10: DAR DE ALTA USUARIOS
    // ========================================

    public String altaUsuario() {
        try {
            // Generar contraseña y guardar usuario
            passwordGenerada = servicioIncidencias.altaUsuario(nuevoUsuario);
            mostrarPassword = true;

            // Mostrar mensaje de éxito
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Usuario creado",
                    "El usuario ha sido dado de alta correctamente."));

            // Limpiar el formulario para el próximo usuario
            nuevoUsuario = new UsuarioDTO();

            // Redirigir a la lista de usuarios (Funcionalidad 11)
            return "listarUsuarios?faces-redirect=true";

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudo crear el usuario: " + e.getMessage()));
            return null;
        }
    }

    public String iniciarAltaUsuario() {
        nuevoUsuario = new UsuarioDTO();
        passwordGenerada = null;
        mostrarPassword = false;
        return "altaUsuario?faces-redirect=true";
    }

    // ========================================
    // FUNCIONALIDAD 11: LISTAR USUARIOS
    // ========================================

    public List<UsuarioDTO> getTodosUsuarios() {
        if (todosUsuarios == null) {
            todosUsuarios = servicioIncidencias.getAllUsuarios();
        }
        return todosUsuarios;
    }

    // ========================================
    // FUNCIONALIDAD 12: ESTADÍSTICAS
    // ========================================

    public void cargarEstadisticas() {
        estadisticasEstado = servicioIncidencias.getIncidenciasPorEstado();
        estadisticasCategoria = servicioIncidencias.getIncidenciasPorCategoria();
        tiempoMedioResolucion = servicioIncidencias.getTiempoMedioResolucion();
        incidenciaMasAntigua = servicioIncidencias.getIncidenciaMasAntiguaAbierta();
    }

    public Map<String, Long> getEstadisticasEstado() {
        if (estadisticasEstado == null) {
            cargarEstadisticas();
        }
        return estadisticasEstado;
    }

    public Map<String, Long> getEstadisticasCategoria() {
        if (estadisticasCategoria == null) {
            cargarEstadisticas();
        }
        return estadisticasCategoria;
    }

    public double getTiempoMedioResolucion() {
        if (estadisticasEstado == null) {
            cargarEstadisticas();
        }
        return tiempoMedioResolucion;
    }

    public IncidenciaDTO getIncidenciaMasAntigua() {
        if (estadisticasEstado == null) {
            cargarEstadisticas();
        }
        return incidenciaMasAntigua;
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

    public String getFiltroEmpleado() {
        return filtroEmpleado;
    }

    public void setFiltroEmpleado(String filtroEmpleado) {
        this.filtroEmpleado = filtroEmpleado;
    }

    public String getFiltroEstado() {
        return filtroEstado;
    }

    public void setFiltroEstado(String filtroEstado) {
        this.filtroEstado = filtroEstado;
    }

    public List<String> getEstadosDisponibles() {
        return estadosDisponibles;
    }

    public UsuarioDTO getNuevoUsuario() {
        return nuevoUsuario;
    }

    public void setNuevoUsuario(UsuarioDTO nuevoUsuario) {
        this.nuevoUsuario = nuevoUsuario;
    }

    public String getPasswordGenerada() {
        return passwordGenerada;
    }

    public boolean isMostrarPassword() {
        return mostrarPassword;
    }

    public List<String> getRolesDisponibles() {
        return rolesDisponibles;
    }

    public List<UsuarioDTO> getEmpleadosParaFiltro() {
        // Devolver solo usuarios normales para el filtro (no incluir admin)
        List<UsuarioDTO> todos = servicioIncidencias.getAllUsuarios();
        todos.removeIf(u -> "ADMIN".equals(u.getRol()));
        return todos;
    }
}