package es.tew.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * Data Transfer Object para la entidad USUARIO.
 * Representa la tabla USUARIO de la base de datos.
 */
public class UsuarioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Campos que corresponden a las columnas de la tabla USUARIO
    private String dni;
    private String nombre;
    private String apellidos;
    private String password; // Usamos 'password' en Java en lugar de 'passwd' por convención
    private String rol; // ADMIN, TECNICO, USUARIO

    // Constructor por defecto (necesario para frameworks como JSF)
    public UsuarioDTO() {
    }

    // Constructor completo
    public UsuarioDTO(String dni, String nombre, String apellidos, String password, String rol) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.password = password;
        this.rol = rol;
    }

    // **********************************************
    // Getters y Setters
    // **********************************************

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    // **********************************************
    // Métodos de utilidad (hashCode, equals, toString)
    // **********************************************

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioDTO that = (UsuarioDTO) o;
        // La igualdad se basa únicamente en la clave primaria (DNI)
        return Objects.equals(dni, that.dni);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dni);
    }

    @Override
    public String toString() {
        return "UsuarioDTO{" +
                "dni='" + dni + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", rol='" + rol + '\'' +
                '}';
    }
}