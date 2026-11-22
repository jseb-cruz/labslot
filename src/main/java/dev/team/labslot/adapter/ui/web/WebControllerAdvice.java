package dev.team.labslot.adapter.ui.web;

import dev.team.labslot.core.constant.UserRole;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Se aplica a todos los controladores de las vistas.
 */
@ControllerAdvice
public class WebControllerAdvice {

    private final UserService userService;

    public WebControllerAdvice(UserService userService) {
        this.userService = userService;
    }

    // Usuario con sesión iniciada, disponible en todas las vistas como ${currentUser}
    // (la barra de navegación muestra su nombre y su rol). Es null en el login.
    // Si un administrador eliminó al usuario mientras tenía la sesión abierta, lo atiende handleDeletedUser
    @ModelAttribute("currentUser")
    public User currentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userService.findByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("El usuario de la sesión ya no existe"));
    }

    // El usuario de la sesión ya no existe: se cierra la sesión y se vuelve al login, que muestra el mensaje
    @ExceptionHandler(UsernameNotFoundException.class)
    public String handleDeletedUser(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, null);
        return "redirect:/login?deleted";
    }

    // Un parámetro de la URL con formato inválido (un estado o un id que no existen) es un error del cliente: 400.
    // Sin este método, Spring usaría handleBusinessError, porque la causa de este error es una
    // IllegalArgumentException, y mostraría un mensaje técnico en inglés como si fuera de negocio.
    @ExceptionHandler(TypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidParameter(Principal principal, Model model) {
        // En los manejadores de errores no se ejecutan los @ModelAttribute: se agrega el usuario a mano
        model.addAttribute("currentUser", currentUser(principal));
        model.addAttribute("status", HttpStatus.BAD_REQUEST.value());
        return "error";
    }

    // Red de seguridad: si un error de negocio no se atrapó en el controlador,
    // se muestra como alerta en la página principal del rol en vez de terminar en la página 500
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleBusinessError(IllegalArgumentException exception, Principal principal,
                                      RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        User user = currentUser(principal);
        if (user != null && user.role() == UserRole.ROLE_ADMIN) {
            return "redirect:/admin/reservations";
        }
        return "redirect:/reservations";
    }
}
