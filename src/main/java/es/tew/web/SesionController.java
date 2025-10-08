package es.tew.web;

import es.tew.dto.UsuarioDTO;
import es.tew.logica.ServicioIncidencias;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;

/**
 * Managed Bean encargado de la gestión de la Sesión del usuario y el Login/Logout.
 * Scope: SessionScoped, para que la instancia persista durante la sesión del usuario.
 */
@Named("sesionController") // Nombre del bean en EL (Expression Language)
@SessionScoped
public class SesionController implements Serializable {

    private static final long serialVersionUID = 1L;

    // Inyección de Dependencias: Acceso a la capa de lógica de negocio
    @Inject
    private ServicioIncidencias servicioIncidencias;

    // Propiedades para el formulario de login
    private String dni;
    private String password;

    // Propiedad que mantiene el usuario autenticado en la sesión
    private UsuarioDTO usuarioActual;

    /**
     * Intenta autenticar al usuario usando el DNI y la contraseña introducidos.
     * @return String de navegación (outcome)
     */
public String login() {
    // 1. Llamar a la capa de lógica para verificar las credenciales
    UsuarioDTO user = servicioIncidencias.login(dni, password);

    if (user != null) {
        // 2. Autenticación exitosa
        this.usuarioActual = user;
        
        // 3. Limpiar campos
        this.dni = null;
        this.password = null;

        // MODIFICADO: Redirección forzada para asegurar la navegación a listado.xhtml
        return "listado?faces-redirect=true"; 
    } else {
        // 4. Autenticación fallida: AÑADIR MENSAJE DE ERROR A JSF
        
        FacesContext.getCurrentInstance().addMessage(
            null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de acceso", "DNI o contraseña incorrectos.")
        );
        
        // Retornar NULL o "" para permanecer en la misma página y mostrar el mensaje
        return null; 
    }
}

    /**
     * Invalida la sesión actual y redirige a la página de login.
     * @return String de navegación (outcome)
     */
    public String logout() {
        this.usuarioActual = null;
        // En una aplicación real, se debería invalidar la sesión HTTP.
        return "exitoLogout?faces-redirect=true"; // Redirección fuerte
    }

    // **********************************************
    // Métodos de control de seguridad (para usar en las páginas XHTML)
    // **********************************************

    public boolean isLogged() {
        return this.usuarioActual != null;
    }
    
    public boolean isAdmin() {
        return isLogged() && "ADMIN".equals(this.usuarioActual.getRol());
    }
    
    public boolean isTecnico() {
        return isLogged() && "TECNICO".equals(this.usuarioActual.getRol());
    }
    
    public boolean isUsuarioNormal() {
        return isLogged() && "USUARIO".equals(this.usuarioActual.getRol());
    }

    // **********************************************
    // Getters y Setters
    // **********************************************

    public UsuarioDTO getUsuarioActual() {
        return usuarioActual;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}