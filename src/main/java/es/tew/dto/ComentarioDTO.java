package es.tew.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Data Transfer Object para la entidad COMENTARIO.
 * Representa la tabla COMENTARIO de la base de datos.
 */
public class ComentarioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Campos simples
    private Long id;
    private String texto;
    private Timestamp fecha; // Correspondiente al tipo TIMESTAMP de SQL

    // Relaciones (Claves Foráneas)
    private IncidenciaDTO incidencia; // ID_INCIDENCIA
    private UsuarioDTO autor;        // DNI_AUTOR

    // Constructor por defecto
    public ComentarioDTO() {
        this.fecha = new Timestamp(System.currentTimeMillis());
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

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public IncidenciaDTO getIncidencia() {
        return incidencia;
    }

    public void setIncidencia(IncidenciaDTO incidencia) {
        this.incidencia = incidencia;
    }

    public UsuarioDTO getAutor() {
        return autor;
    }

    public void setAutor(UsuarioDTO autor) {
        this.autor = autor;
    }
    
    // **********************************************
    // Métodos de utilidad (hashCode, equals, toString)
    // **********************************************

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComentarioDTO that = (ComentarioDTO) o;
        // La igualdad se basa en la clave primaria (ID)
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ComentarioDTO{" +
                "id=" + id +
                ", incidenciaId=" + (incidencia != null ? incidencia.getId() : "N/A") +
                ", autorDNI='" + (autor != null ? autor.getDni() : "N/A") + '\'' +
                ", texto='" + texto.substring(0, Math.min(texto.length(), 30)) + (texto.length() > 30 ? "..." : "") + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}