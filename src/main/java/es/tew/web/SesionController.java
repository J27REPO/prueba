package es.tew.web;

import java.io.Serializable;

import es.tew.dto.UsuarioDTO;
import es.tew.logica.ServicioIncidencias;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Managed Bean encargado de la gestión de la Sesión del usuario y el
 * Login/Logout.
 * Scope: SessionScoped, para que la instancia persista durante la sesión del
 * usuario.
 */
@Named("sesionController")
@SessionScoped
public class SesionController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ServicioIncidencias servicioIncidencias;

    // Propiedades para el formulario de login
    private String dni;
    private String password;

    // Propiedad que mantiene el usuario autenticado en la sesión
    private UsuarioDTO usuarioActual;

    /**
     * Intenta autenticar al usuario usando el DNI y la contraseña introducidos.
     * 
     * @return String de navegación (outcome)
     */
    public String login() {
        // VALIDACIÓN BÁSICA
        if (!validarDNI(dni)) {
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de acceso", "El DNI introducido no es válido."));
            return null;
        }

        // AUTENTICACIÓN A TRAVÉS DEL SERVICIO
        UsuarioDTO user = servicioIncidencias.login(dni, password);

        if (user != null) {
            // Autenticación exitosa
            this.usuarioActual = user;
            
            // Limpiar campos
            this.dni = null;
            this.password = null;
            
            // Redirección forzada a listado
            return "listado?faces-redirect=true";
        } else {
            // Autenticación fallida
            FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de acceso", "DNI o contraseña incorrectos."));
            return null;
        }
    }

    /**
     * Valida el formato del DNI español.
     * @param dni DNI a validar
     * @return true si es válido, false en caso contrario
     */
    private boolean validarDNI(String dni) {
        if (dni == null || dni.length() != 9) {
            return false;
        }

        String numberPart = dni.substring(0, 8);
        char letterPart = Character.toUpperCase(dni.charAt(8));

        if (!numberPart.matches("\\d+")) {
            return false;
        }

        // Cálculo de la letra del DNI español
        String letters = "TRWAGMYFPDXBNJZSQVHLCKE";
        int number = Integer.parseInt(numberPart);
        char correctLetter = letters.charAt(number % 23);

        return letterPart == correctLetter;
    }

    /**
     * Invalida la sesión actual y redirige a la página de login.
     * 
     * @return String de navegación (outcome)
     */
    public String logout() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    // **********************************************
    // Métodos de control de seguridad
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
    public void setUsuarioActual(UsuarioDTO usuarioActual) {
    this.usuarioActual = usuarioActual;
    if (usuarioActual != null) {
        FacesContext.getCurrentInstance().getExternalContext()
            .getSessionMap().put("usuarioActual", usuarioActual);
    }
}
}