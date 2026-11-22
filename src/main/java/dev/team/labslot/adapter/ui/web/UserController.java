package dev.team.labslot.adapter.ui.web;

import dev.team.labslot.core.constant.UserRole;
import dev.team.labslot.core.dto.UserDTO;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

/**
 * Vistas del administrador para los usuarios: listar, crear y eliminar.
 */
@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll().stream()
                .sorted(Comparator.comparing(User::fullName))
                .toList());
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("user", new UserDTO("", "", null, UserRole.ROLE_USER));
        return "admin/users/form";
    }

    @PostMapping
    public String create(@ModelAttribute("user") UserDTO user,
                         BindingResult bindingResult,
                         @RequestParam(required = false) String passwordConfirmation,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        // Datos con formato inválido (por ejemplo, un rol que no existe): no se alcanzan a convertir al DTO
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Revisa el formulario: hay campos vacíos o con un formato inválido");
            return "admin/users/form";
        }
        // La confirmación solo existe en el formulario: no es un dato del usuario, por eso se compara aquí
        if (!Objects.equals(user.password(), passwordConfirmation)) {
            return showError("Las contraseñas no coinciden", user, model);
        }
        try {
            userService.create(user);
        } catch (IllegalArgumentException exception) {
            return showError(exception.getMessage(), user, model);
        }
        redirectAttributes.addFlashAttribute("successMessage", "Usuario " + user.email() + " creado");
        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/delete")
    public String delete(@PathVariable UUID userId,
                         @ModelAttribute(name = "currentUser", binding = false) User currentUser,
                         RedirectAttributes redirectAttributes) {
        try {
            // El servicio comprueba que no sea el propio administrador ni un usuario con reservas
            userService.delete(userId, currentUser.userId());
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/users";
    }

    // Vuelve al formulario con la alerta y lo que se escribió, menos las contraseñas
    private String showError(String message, UserDTO user, Model model) {
        model.addAttribute("errorMessage", message);
        model.addAttribute("user", new UserDTO(user.fullName(), user.email(), null, user.role()));
        return "admin/users/form";
    }
}
