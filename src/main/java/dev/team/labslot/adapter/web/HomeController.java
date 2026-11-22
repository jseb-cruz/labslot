package dev.team.labslot.adapter.web;

import dev.team.labslot.core.constant.UserRole;
import dev.team.labslot.core.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HomeController {

    // Página de inicio: cada rol va a su pantalla principal
    @GetMapping("/")
    public String home(@ModelAttribute(name = "currentUser", binding = false) User currentUser) {
        if (currentUser.role() == UserRole.ROLE_ADMIN) {
            return "redirect:/admin/reservations";
        }
        return "redirect:/reservations";
    }

    // Formulario de login; el POST /login lo procesa Spring Security
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
