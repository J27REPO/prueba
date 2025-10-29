package es.tew.web;

import java.io.Serializable;
import java.util.Locale;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 * Controlador para la gestión de la internacionalización (i18n).
 * Permite cambiar el idioma de la aplicación.
 */
@Named("localeController")
@SessionScoped
public class LocaleController implements Serializable {

    private static final long serialVersionUID = 1L;

    private Locale locale = new Locale("es"); // Idioma por defecto: español

    /**
     * Obtiene el locale actual.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Obtiene el código del idioma actual (para mostrar en la UI).
     */
    public String getLanguage() {
        return locale.getLanguage();
    }

    /**
     * Cambia el idioma a español.
     */
    public void cambiarAEspanol() {
        locale = new Locale("es");
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }

    /**
     * Cambia el idioma a inglés.
     */
    public void cambiarAIngles() {
        locale = new Locale("en");
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }

    /**
     * Cambia el idioma de forma genérica.
     * @param language Código del idioma ("es" o "en")
     */
    public void cambiarIdioma(String language) {
        locale = new Locale(language);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }
}