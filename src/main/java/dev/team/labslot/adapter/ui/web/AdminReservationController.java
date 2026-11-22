package dev.team.labslot.adapter.ui.web;

import dev.team.labslot.core.constant.ReservationState;
import dev.team.labslot.core.entity.Laboratory;
import dev.team.labslot.core.entity.Reservation;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.service.LaboratoryService;
import dev.team.labslot.core.service.ReservationService;
import dev.team.labslot.core.service.UserService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Vistas del administrador para las reservas: listar con filtro por estado y cambiar su estado.
 */
@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final LaboratoryService laboratoryService;
    private final UserService userService;
    private final Clock clock;

    public AdminReservationController(ReservationService reservationService, LaboratoryService laboratoryService,
                                      UserService userService, Clock clock) {
        this.reservationService = reservationService;
        this.laboratoryService = laboratoryService;
        this.userService = userService;
        this.clock = clock;
    }

    // state vacío ("Todas") llega como null
    @GetMapping
    public String list(@RequestParam(required = false) ReservationState state, Model model) {
        List<Reservation> reservations = reservationService.findAll().stream()
                .filter(reservation -> state == null || reservation.state() == state)
                .sorted(Comparator.comparing(Reservation::date).thenComparing(Reservation::startTime))
                .toList();
        model.addAttribute("reservations", reservations);
        model.addAttribute("selectedState", state);
        // La reserva solo guarda ids; con estos mapas la vista muestra el laboratorio y el solicitante
        model.addAttribute("laboratories", laboratoryService.findAll().stream()
                .collect(Collectors.toMap(Laboratory::laboratoryId, Function.identity())));
        model.addAttribute("users", userService.findAll().stream()
                .collect(Collectors.toMap(User::userId, Function.identity())));
        // Fechas del formulario del reporte: el mes actual, salvo que vengan de un intento con error
        LocalDate today = LocalDate.now(clock);
        if (!model.containsAttribute("reportStartDate")) {
            model.addAttribute("reportStartDate", today.withDayOfMonth(1));
        }
        if (!model.containsAttribute("reportEndDate")) {
            model.addAttribute("reportEndDate", today.with(TemporalAdjusters.lastDayOfMonth()));
        }
        return "admin/reservations";
    }

    // Descarga el reporte PDF de reservas entre dos fechas.
    // Devuelve el PDF (ResponseEntity) o, si el rango no es válido, vuelve a la lista con la alerta (redirect)
    @GetMapping("/report")
    public Object report(@RequestParam(required = false) LocalDate startDate,
                         @RequestParam(required = false) LocalDate endDate,
                         RedirectAttributes redirectAttributes) {
        try {
            byte[] pdf = reservationService.generateReport(startDate, endDate);
            String fileName = "reservas_" + startDate + "_" + endDate + ".pdf";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(fileName).build().toString())
                    .body(pdf);
        } catch (IllegalArgumentException exception) {
            // Se conservan las fechas que escribió el administrador
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
            redirectAttributes.addFlashAttribute("reportStartDate", startDate);
            redirectAttributes.addFlashAttribute("reportEndDate", endDate);
            return "redirect:/admin/reservations";
        }
    }

    // En las acciones, "filter" es el filtro que estaba aplicado, para volver a la misma lista
    @PostMapping("/{reservationId}/approve")
    public String approve(@PathVariable UUID reservationId, @RequestParam(required = false) ReservationState filter,
                          RedirectAttributes redirectAttributes) {
        try {
            reservationService.approve(reservationId);
            redirectAttributes.addFlashAttribute("successMessage", "Reserva aprobada");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return redirectToList(filter, redirectAttributes);
    }

    @PostMapping("/{reservationId}/reject")
    public String reject(@PathVariable UUID reservationId, @RequestParam(required = false) ReservationState filter,
                         RedirectAttributes redirectAttributes) {
        try {
            reservationService.reject(reservationId);
            redirectAttributes.addFlashAttribute("successMessage", "Reserva rechazada");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return redirectToList(filter, redirectAttributes);
    }

    @PostMapping("/{reservationId}/complete")
    public String complete(@PathVariable UUID reservationId, @RequestParam(required = false) ReservationState filter,
                           RedirectAttributes redirectAttributes) {
        try {
            reservationService.complete(reservationId);
            redirectAttributes.addFlashAttribute("successMessage", "Reserva completada");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return redirectToList(filter, redirectAttributes);
    }

    // El administrador cancela cualquier reserva: se pasa su propio id como quien cancela
    @PostMapping("/{reservationId}/cancel")
    public String cancel(@PathVariable UUID reservationId, @RequestParam(required = false) ReservationState filter,
                         @ModelAttribute(name = "currentUser", binding = false) User currentUser,
                         RedirectAttributes redirectAttributes) {
        try {
            reservationService.cancel(reservationId, currentUser.userId());
            redirectAttributes.addFlashAttribute("successMessage", "Reserva cancelada");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return redirectToList(filter, redirectAttributes);
    }

    private String redirectToList(ReservationState filter, RedirectAttributes redirectAttributes) {
        if (filter != null) {
            redirectAttributes.addAttribute("state", filter);
        }
        return "redirect:/admin/reservations";
    }
}
