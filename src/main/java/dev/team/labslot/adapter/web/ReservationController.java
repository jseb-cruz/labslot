package dev.team.labslot.adapter.web;

import dev.team.labslot.core.constant.Constants;
import dev.team.labslot.core.constant.LaboratoryState;
import dev.team.labslot.core.dto.ReservationDTO;
import dev.team.labslot.core.entity.Laboratory;
import dev.team.labslot.core.entity.Reservation;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.service.LaboratoryService;
import dev.team.labslot.core.service.ReservationService;
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

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Vistas del usuario (ROLE_USER): sus reservas, crear una reserva y cancelar las suyas.
 */
@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final LaboratoryService laboratoryService;
    private final Clock clock;

    public ReservationController(ReservationService reservationService, LaboratoryService laboratoryService, Clock clock) {
        this.reservationService = reservationService;
        this.laboratoryService = laboratoryService;
        this.clock = clock;
    }

    @GetMapping
    public String list(@ModelAttribute(name = "currentUser", binding = false) User currentUser, Model model) {
        List<Reservation> reservations = reservationService.findByUser(currentUser.userId()).stream()
                .sorted(Comparator.comparing(Reservation::date).thenComparing(Reservation::startTime))
                .toList();
        model.addAttribute("reservations", reservations);
        // La reserva solo guarda el id del laboratorio; con este mapa la vista muestra su código y nombre
        model.addAttribute("laboratories", laboratoryService.findAll().stream()
                .collect(Collectors.toMap(Laboratory::laboratoryId, Function.identity())));
        return "reservations/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("reservation", new ReservationDTO(null, null, null, 1, "", null));
        return showForm(model, null);
    }

    @PostMapping
    public String create(@ModelAttribute("reservation") ReservationDTO reservation,
                         BindingResult bindingResult,
                         @RequestParam(required = false) UUID laboratoryId,
                         @ModelAttribute(name = "currentUser", binding = false) User currentUser,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        // Datos con formato inválido (por ejemplo, asistentes vacío): no se alcanzan a convertir al DTO
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Revisa el formulario: hay campos vacíos o con un formato inválido");
            return showForm(model, laboratoryId);
        }
        try {
            reservationService.create(laboratoryId, currentUser.userId(), reservation);
        } catch (IllegalArgumentException exception) {
            // Error de negocio: se vuelve al formulario con la alerta y lo que el usuario escribió
            model.addAttribute("errorMessage", exception.getMessage());
            return showForm(model, laboratoryId);
        }
        redirectAttributes.addFlashAttribute("successMessage", "Reserva creada. Queda pendiente de aprobación.");
        return "redirect:/reservations";
    }

    @PostMapping("/{reservationId}/cancel")
    public String cancel(@PathVariable UUID reservationId,
                         @ModelAttribute(name = "currentUser", binding = false) User currentUser,
                         RedirectAttributes redirectAttributes) {
        try {
            // El servicio comprueba que la reserva sea del usuario
            reservationService.cancel(reservationId, currentUser.userId());
            redirectAttributes.addFlashAttribute("successMessage", "Reserva cancelada");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/reservations";
    }

    // Datos que necesita el formulario además de la reserva: laboratorios activos, el seleccionado y límites
    private String showForm(Model model, UUID selectedLaboratoryId) {
        model.addAttribute("activeLaboratories", laboratoryService.findAll().stream()
                .filter(laboratory -> laboratory.state() == LaboratoryState.LSTATE_ACTIVE)
                .sorted(Comparator.comparing(Laboratory::code))
                .toList());
        model.addAttribute("selectedLaboratoryId", selectedLaboratoryId);
        model.addAttribute("today", LocalDate.now(clock));
        model.addAttribute("openingTime", Constants.OPENING_TIME);
        model.addAttribute("closingTime", Constants.CLOSING_TIME);
        model.addAttribute("maxHours", Constants.MAX_RESERVATION_HOURS);
        return "reservations/form";
    }
}
