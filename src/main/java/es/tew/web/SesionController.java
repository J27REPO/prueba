package es.tew.web;

import es.tew.dto.UsuarioDTO;
import es.tew.logica.ServicioIncidencias;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Managed Bean encargado de la gestión de la Sesión del usuario y el
 * Login/Logout.
 * Scope: SessionScoped, para que la instancia persista durante la sesión del
 * usuario.
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
     * 
     * @return String de navegación (outcome)
     */
    public String login() {
        // 1. Llamar a la capa de lógica para verificar las credenciales
        UsuarioDTO user = servicioIncidencias.login(dni, password);
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String role = null;
        if (user != null) {
            if (validador(dni)) {
                try {
 
                    // 1. Conectar a la base de datos
                    conn = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/localDB", "sa", "");
                    System.out.println("hola\n ");
                    // 2. Consulta para obtener el usuario por DNI
                    String sql = "SELECT PASSWD, ROL FROM USUARIO WHERE DNI = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, dni);
                    rs = stmt.executeQuery();

                    if (rs.next()) {
                        String storedPassword = rs.getString("PASSWD");
                        role = rs.getString("ROL");
                        System.out.println("contraseña: " + storedPassword +"\n");
                        // 3. Verificar contraseña
                        if (storedPassword.equals(password)) {
                            // Credenciales correctas
                            System.out.println("Role devuelto por servicio: " + role);
                            return role;
                             // Coinciden
                        } else {
                            // Contraseña incorrecta, devuelve el rol
                            return null;
                        }
                    } else {
                        // DNI no existe en la base de datos
                        return null;
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    // 4. Cerrar recursos
                    try {
                        if (rs != null)
                            rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (stmt != null)
                            stmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (conn != null)
                            conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            }

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
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de acceso", "DNI o contraseña incorrectos."));

            // Retornar NULL o "" para permanecer en la misma página y mostrar el mensaje
            return null;
        }
    }

    private boolean validador(String dni2) {
        if (dni2 == null || dni2.length() != 9) {
            return false; // Invalid length
        }

        String numberPart = dni2.substring(0, 8);
        char letterPart = Character.toUpperCase(dni2.charAt(8));

        if (!numberPart.matches("\\d+")) {
            return false; // First 8 characters must be digits
        }

        // Spanish DNI letter calculation
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
        // 1. Obtener el contexto externo de JSF
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();

        // 2. Invalidar la sesión HTTP actual. Esto destruye el bean de sesión.
        ec.invalidateSession();

        // 3. Redirigir directamente a la página de login.
        return "/login.xhtml?faces-redirect=true";
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