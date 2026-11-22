package dev.team.labslot.adapter.ui.api;

import dev.team.labslot.core.constant.LaboratoryState;
import dev.team.labslot.core.dto.LaboratoryDTO;
import dev.team.labslot.core.entity.Laboratory;
import dev.team.labslot.core.service.LaboratoryService;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.UUID;

/**
 * Handlers de la API REST para laboratorios. Solo llaman a LaboratoryService.
 */
@Component
public class LaboratoryRestController {

    private final LaboratoryService laboratoryService;

    public LaboratoryRestController(LaboratoryService laboratoryService) {
        this.laboratoryService = laboratoryService;
    }

    // GET /api/laboratories
    public void findAll(Context ctx) {
        ctx.json(laboratoryService.findAll().stream()
                .sorted(Comparator.comparing(Laboratory::code))
                .toList());
    }

    // GET /api/laboratories/{laboratoryId}
    public void findById(Context ctx) {
        ctx.json(findOrNotFound(laboratoryId(ctx)));
    }

    // POST /api/laboratories (ADMIN). Cuerpo: LaboratoryDTO
    public void create(Context ctx) {
        Laboratory laboratory = laboratoryService.create(ctx.bodyAsClass(LaboratoryDTO.class));
        ctx.header("Location", "/api/laboratories/" + laboratory.laboratoryId());
        ctx.status(201).json(laboratory);
    }

    // POST /api/laboratories/{laboratoryId}/toggle-state (ADMIN): activo <-> mantenimiento
    public void toggleState(Context ctx) {
        Laboratory laboratory = findOrNotFound(laboratoryId(ctx));
        LaboratoryState newState = laboratory.state() == LaboratoryState.LSTATE_ACTIVE
                ? LaboratoryState.LSTATE_MAINTENANCE
                : LaboratoryState.LSTATE_ACTIVE;
        ctx.json(laboratoryService.update(laboratory.laboratoryId(), new LaboratoryDTO(
                laboratory.code(),
                laboratory.name(),
                laboratory.building(),
                laboratory.capacity(),
                newState
        )).orElseThrow(() -> new NotFoundResponse("No existe el laboratorio")));
    }

    private Laboratory findOrNotFound(UUID laboratoryId) {
        return laboratoryService.findById(laboratoryId)
                .orElseThrow(() -> new NotFoundResponse("No existe el laboratorio"));
    }

    // Convierte el id de la ruta; si no es un UUID, error 400 con mensaje en español
    private UUID laboratoryId(Context ctx) {
        try {
            return UUID.fromString(ctx.pathParam("laboratoryId"));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("El id del laboratorio no es válido");
        }
    }
}
