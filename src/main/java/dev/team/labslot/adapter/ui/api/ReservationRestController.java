package dev.team.labslot.adapter.ui.api;

import dev.team.labslot.core.constant.ReservationState;
import dev.team.labslot.core.constant.UserRole;
import dev.team.labslot.core.dto.ReservationDTO;
import dev.team.labslot.core.entity.Reservation;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.service.LaboratoryService;
import dev.team.labslot.core.service.ReservationService;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.NotFoundResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Handlers de la API REST para reservas. Solo llaman a los servicios.
 * El usuario autenticado lo deja JavalinServer en ctx.attribute("currentUser").
 */
@Component
public class ReservationRestController {

    private final ReservationService reservationService;
    private final LaboratoryService laboratoryService;

    public ReservationRestController(ReservationService reservationService, LaboratoryService laboratoryService) {
        this.reservationService = reservationService;
        this.laboratoryService = laboratoryService;
    }

    // GET /api/reservations[?state=RSTATE_PENDING]: el administrador ve todas; el usuario, solo las suyas
    public void findAll(Context ctx) {
        User currentUser = ctx.attribute("currentUser");
        ReservationState state = stateFilter(ctx);
        List<Reservation> reservations = isAdmin(currentUser)
                ? reservationService.findAll()
                : reservationService.findByUser(currentUser.userId());
        ctx.json(reservations.stream()
                .filter(reservation -> state == null || reservation.state() == state)
                .sorted(Comparator.comparing(Reservation::date).thenComparing(Reservation::startTime))
                .toList());
    }

    // GET /api/reservations/{reservationId}: el usuario solo puede ver las suyas (403 si no)
    public void findById(Context ctx) {
        User currentUser = ctx.attribute("currentUser");
        Reservation reservation = findOrNotFound(reservationId(ctx));
        if (!isAdmin(currentUser) && !reservation.userId().equals(currentUser.userId())) {
            throw new ForbiddenResponse("No puedes ver una reserva de otro usuario");
        }
        ctx.json(reservation);
    }

    // POST /api/laboratories/{laboratoryId}/reservations (USER). Cuerpo: ReservationDTO (el estado se ignora)
    public void create(Context ctx) {
        User currentUser = ctx.attribute("currentUser");
        UUID laboratoryId = laboratoryId(ctx);
        if (laboratoryService.findById(laboratoryId).isEmpty()) {
            throw new NotFoundResponse("No existe el laboratorio");
        }
        Reservation reservation = reservationService.create(laboratoryId, currentUser.userId(),
                ctx.bodyAsClass(ReservationDTO.class));
        ctx.header("Location", "/api/reservations/" + reservation.reservationId());
        ctx.status(201).json(reservation);
    }

    // POST /api/reservations/{reservationId}/cancel: el servicio valida que sea el dueño o un administrador
    public void cancel(Context ctx) {
        User currentUser = ctx.attribute("currentUser");
        UUID reservationId = findOrNotFound(reservationId(ctx)).reservationId();
        ctx.json(reservationService.cancel(reservationId, currentUser.userId()));
    }

    // POST /api/reservations/{reservationId}/approve (ADMIN)
    public void approve(Context ctx) {
        ctx.json(reservationService.approve(findOrNotFound(reservationId(ctx)).reservationId()));
    }

    // POST /api/reservations/{reservationId}/reject (ADMIN)
    public void reject(Context ctx) {
        ctx.json(reservationService.reject(findOrNotFound(reservationId(ctx)).reservationId()));
    }

    // POST /api/reservations/{reservationId}/complete (ADMIN)
    public void complete(Context ctx) {
        ctx.json(reservationService.complete(findOrNotFound(reservationId(ctx)).reservationId()));
    }

    // Recurso inexistente: 404 (y no el 400 que daría el servicio)
    private Reservation findOrNotFound(UUID reservationId) {
        return reservationService.findById(reservationId)
                .orElseThrow(() -> new NotFoundResponse("No existe la reserva"));
    }

    private boolean isAdmin(User user) {
        return user.role() == UserRole.ROLE_ADMIN;
    }

    // Filtro opcional ?state=; si no es un estado válido, error 400 con mensaje en español
    private ReservationState stateFilter(Context ctx) {
        String state = ctx.queryParam("state");
        if (state == null || state.isBlank()) {
            return null;
        }
        try {
            return ReservationState.valueOf(state);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("El estado de la reserva no es válido");
        }
    }

    // Convierten los ids de la ruta; si no son UUID, error 400 con mensaje en español
    private UUID reservationId(Context ctx) {
        try {
            return UUID.fromString(ctx.pathParam("reservationId"));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("El id de la reserva no es válido");
        }
    }

    private UUID laboratoryId(Context ctx) {
        try {
            return UUID.fromString(ctx.pathParam("laboratoryId"));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("El id del laboratorio no es válido");
        }
    }
}
