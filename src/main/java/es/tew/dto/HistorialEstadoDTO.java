package es.tew.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Data Transfer Object para la entidad HISTORIAL_ESTADO.
 * Representa la tabla HISTORIAL_ESTADO de la base de datos, 
 * registrando los cambios de estado de una Incidencia.
 */
public class HistorialEstadoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Campos simples
    private Long id;
    private Timestamp fechaCambio; // Correspondiente al tipo TIMESTAMP de SQL
    private String estadoAnterior; // Puede ser NULL si es el primer estado (Abierta)
    private String estadoNuevo;    // Nuevo estado de la incidencia

    // Relaciones (Claves Foráneas)
    private IncidenciaDTO incidencia; // ID_INCIDENCIA
    private UsuarioDTO usuario;      // DNI_USUARIO (quién realizó el cambio)

    // Constructor por defecto
    public HistorialEstadoDTO() {
        this.fechaCambio = new Timestamp(System.currentTimeMillis());
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

    public Timestamp getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(Timestamp fechaCambio) {
        this.fechaCambio = fechaCambio;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(String estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public IncidenciaDTO getIncidencia() {
        return incidencia;
    }

    public void setIncidencia(IncidenciaDTO incidencia) {
        this.incidencia = incidencia;
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioDTO usuario) {
        this.usuario = usuario;
    }

    // **********************************************
    // Métodos de utilidad (hashCode, equals, toString)
    // **********************************************
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistorialEstadoDTO that = (HistorialEstadoDTO) o;
        // La igualdad se basa en la clave primaria (ID)
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "HistorialEstadoDTO{" +
                "id=" + id +
                ", incidenciaId=" + (incidencia != null ? incidencia.getId() : "N/A") +
                ", estadoAnterior='" + estadoAnterior + '\'' +
                ", estadoNuevo='" + estadoNuevo + '\'' +
                ", usuarioDNI='" + (usuario != null ? usuario.getDni() : "N/A") + '\'' +
                ", fechaCambio=" + fechaCambio +
                '}';
    }
}