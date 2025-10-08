package es.tew.web;

import es.tew.dto.UsuarioDTO;
import es.tew.logica.ServicioIncidencias;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import java.util.Objects;

/**
 * Conversor para la entidad UsuarioDTO.
 * Permite a JSF convertir el objeto UsuarioDTO en String (DNI) y viceversa.
 */
@Named
@FacesConverter(value = "usuarioConverter", managed = true) // 'managed=true' permite la inyección de dependencias (CDI)
public class UsuarioConverter implements Converter<UsuarioDTO> {

    // Inyectamos la capa de lógica para buscar el objeto por su clave (DNI)
    @Inject
    private ServicioIncidencias servicioIncidencias;

    /**
     * Convierte el objeto UsuarioDTO en su representación String (el DNI).
     * Usado al renderizar el <p:selectOneMenu>.
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, UsuarioDTO value) {
        if (value == null) {
            return null;
        }
        // Devolvemos el DNI, ya que es la clave única (identificador)
        return value.getDni(); 
    }

    /**
     * Convierte la representación String (el DNI) en el objeto UsuarioDTO.
     * Usado cuando se envía el formulario (al pulsar "Guardar Cambios").
     */
    @Override
    public UsuarioDTO getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // 1. Verificar si el valor es el identificador "nulo" (si se selecciona "Sin Asignar")
        if (Objects.equals(value, "null")) {
            return null;
        }

        try {
            // 2. Usar la capa de lógica para buscar el UsuarioDTO por el DNI
            return servicioIncidencias.getUsuarioByDni(value);
        } catch (Exception e) {
            System.err.println("Error en UsuarioConverter al buscar DNI: " + value);
            e.printStackTrace();
            return null;
        }
    }
}