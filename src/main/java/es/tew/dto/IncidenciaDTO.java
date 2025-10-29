package es.tew.dto;

import java.io.Serializable;
import java.sql.Timestamp; // Usaremos java.sql.Timestamp para coincidir con el campo de la BDD
import java.util.Objects;

/**
 * Data Transfer Object para la entidad INCIDENCIA.
 * Representa la tabla INCIDENCIA de la base de datos, 
 * incluyendo las relaciones con la tabla USUARIO.
 */
public class IncidenciaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Campos simples (columnas directas de la tabla INCIDENCIA)
    private Long id;
    private String titulo;
    private String descripcion;
    private Timestamp fechaCreacion; // Correspondiente al tipo TIMESTAMP de SQL
    private String estado; // Abierta, Asignada, En Proceso, Cerrada
    private String categoria;

    // Relaciones (FOREIGN KEYs)
    // En el DTO, las relaciones a menudo se mapean a los objetos DTO relacionados
    private UsuarioDTO solicitante; // DNI_SOLICITANTE
    private UsuarioDTO tecnico;     // DNI_TECNICO (Puede ser null)

    // Constructor por defecto
    public IncidenciaDTO() {
        this.fechaCreacion = new Timestamp(System.currentTimeMillis());
    }

    // **********************************************
    // Getters y Setters
    // **********************************************

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public UsuarioDTO getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(UsuarioDTO solicitante) {
        this.solicitante = solicitante;
    }

    public UsuarioDTO getTecnico() {
        return tecnico;
    }

    public void setTecnico(UsuarioDTO tecnico) {
        this.tecnico = tecnico;
    }

    // **********************************************
    // Métodos de utilidad (hashCode, equals, toString)
    // **********************************************

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IncidenciaDTO that = (IncidenciaDTO) o;
        // La igualdad se basa en la clave primaria (ID)
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "IncidenciaDTO{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", estado='" + estado + '\'' +
                ", solicitanteDNI='" + (solicitante != null ? solicitante.getDni() : "N/A") + '\'' +
                '}';
    }
}