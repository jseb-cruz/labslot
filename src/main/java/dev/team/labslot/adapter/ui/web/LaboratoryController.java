package dev.team.labslot.adapter.ui.web;

import dev.team.labslot.core.constant.LaboratoryState;
import dev.team.labslot.core.dto.LaboratoryDTO;
import dev.team.labslot.core.entity.Laboratory;
import dev.team.labslot.core.service.LaboratoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.UUID;

/**
 * Vistas del administrador para los laboratorios: listar, crear y cambiar entre activo y mantenimiento.
 */
@Controller
@RequestMapping("/admin/laboratories")
public class LaboratoryController {

    private final LaboratoryService laboratoryService;

    public LaboratoryController(LaboratoryService laboratoryService) {
        this.laboratoryService = laboratoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("laboratories", laboratoryService.findAll().stream()
                .sorted(Comparator.comparing(Laboratory::code))
                .toList());
        return "admin/laboratories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("laboratory", new LaboratoryDTO("", "", "", 1, LaboratoryState.LSTATE_ACTIVE));
        return "admin/laboratories/form";
    }

    @PostMapping
    public String create(@ModelAttribute("laboratory") LaboratoryDTO laboratory,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        // Datos con formato inválido (por ejemplo, capacidad vacía): no se alcanzan a convertir al DTO
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Revisa el formulario: hay campos vacíos o con un formato inválido");
            return "admin/laboratories/form";
        }
        try {
            laboratoryService.create(laboratory);
        } catch (IllegalArgumentException exception) {
            // Error de negocio: se vuelve al formulario con la alerta y lo que el usuario escribió
            model.addAttribute("errorMessage", exception.getMessage());
            return "admin/laboratories/form";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Laboratorio " + laboratory.code() + " creado");
        return "redirect:/admin/laboratories";
    }

    // Cambia el laboratorio entre activo y mantenimiento conservando el resto de sus datos
    @PostMapping("/{laboratoryId}/toggle-state")
    public String toggleState(@PathVariable UUID laboratoryId, RedirectAttributes redirectAttributes) {
        try {
            Laboratory laboratory = laboratoryService.findById(laboratoryId)
                    .orElseThrow(() -> new IllegalArgumentException("No existe el laboratorio"));
            LaboratoryState newState = laboratory.state() == LaboratoryState.LSTATE_ACTIVE
                    ? LaboratoryState.LSTATE_MAINTENANCE
                    : LaboratoryState.LSTATE_ACTIVE;
            laboratoryService.update(laboratoryId, new LaboratoryDTO(
                    laboratory.code(),
                    laboratory.name(),
                    laboratory.building(),
                    laboratory.capacity(),
                    newState
            ));
            String stateText = newState == LaboratoryState.LSTATE_ACTIVE ? "activo" : "en mantenimiento";
            redirectAttributes.addFlashAttribute("successMessage",
                    "El laboratorio " + laboratory.code() + " quedó " + stateText);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/laboratories";
    }
}
