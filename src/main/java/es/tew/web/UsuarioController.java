package es.tew.web;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.List;
import es.tew.dao.UsuarioDaoJdbc;
import es.tew.dto.UsuarioDTO;

@Named
@SessionScoped
public class UsuarioController implements Serializable {

    private static final long serialVersionUID = 1L;

    private UsuarioDaoJdbc usuarioDAO = new UsuarioDaoJdbc();


    private UsuarioDTO nuevoEmpleado = new UsuarioDTO();
    private List<UsuarioDTO> listaUsuarios; // Lista de usuarios para mostrar en la vista
    public UsuarioDTO getNuevoEmpleado() {
        return nuevoEmpleado;
    }

    public void setNuevoEmpleado(UsuarioDTO nuevoEmpleado) {
        this.nuevoEmpleado = nuevoEmpleado;
    }

    public List<UsuarioDTO> getListaUsuarios() {
        return listaUsuarios;
    }

    public void setListaUsuarios(List<UsuarioDTO> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }
@PostConstruct
public void init() {
    listaUsuarios(); // Llama al método de carga
}
    /**
     * Guarda un nuevo empleado en la base de datos.
     * Genera una contraseña automáticamente y la asigna al usuario.
     */
    public String guardarEmpleado() {
        try {
            // Generar contraseña aleatoria
            String contrasenaGenerada = generarContrasena();
            nuevoEmpleado.setPassword(contrasenaGenerada);

            // Guardar el usuario en la base de datos
            usuarioDAO.save(nuevoEmpleado);

            // Actualizar la lista de usuarios para reflejar el cambio
            listaUsuarios();

            // Mostrar mensaje de éxito (puede usar FacesContext)
            System.out.println("Empleado creado. Contraseña generada: " + contrasenaGenerada);
            System.out.println("rol generada: " + nuevoEmpleado.getRol());
            this.nuevoEmpleado = new UsuarioDTO();
            
            return "/listadoUsuarios.xhtml?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            // Aquí se podría agregar FacesMessage para mostrar error en la vista
            return null;
        }
    }

    /**
     * Genera una contraseña aleatoria de longitud dada.
     */
    private String generarContrasena() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder contrasena = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            int indice = random.nextInt(caracteres.length());
            contrasena.append(caracteres.charAt(indice));
        }
        return contrasena.toString();

    }

    /**
     * Recupera todos los usuarios de la base de datos.
     */
    public List<UsuarioDTO> listaUsuarios() {
        try {
            listaUsuarios = usuarioDAO.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            listaUsuarios = null; // en caso de error, limpiar lista
        }
        return listaUsuarios;
    }


}
