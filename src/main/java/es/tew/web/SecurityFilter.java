package es.tew.web;

import java.io.IOException;

import es.tew.dto.UsuarioDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter(urlPatterns = {"*.xhtml"})
public class SecurityFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        
        String requestURI = req.getRequestURI();
        
        // Páginas públicas
        if (requestURI.endsWith("/login.xhtml") || 
            requestURI.contains("/jakarta.faces.resource/")) {
            chain.doFilter(request, response);
            return;
        }
        
        // Verificar sesión
        if (session == null || session.getAttribute("usuarioActual") == null) {
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }
        
        // Verificar acceso a páginas de admin
        if (requestURI.contains("/admin/")) {
            UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioActual");
            if (!"ADMIN".equals(usuario.getRol())) {
                res.sendRedirect(req.getContextPath() + "/listado.xhtml");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}