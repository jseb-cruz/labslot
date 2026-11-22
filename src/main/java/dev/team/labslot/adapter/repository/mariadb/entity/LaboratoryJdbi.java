package dev.team.labslot.adapter.repository.mariadb.entity;

import dev.team.labslot.core.constant.LaboratoryState;
import dev.team.labslot.core.entity.Laboratory;

import java.util.UUID;

/**
 * Fila de la tabla laboratories. El id va como String porque la columna es VARCHAR(36).
 */
public record LaboratoryJdbi(
        String laboratoryId,
        String code,
        String name,
        String building,
        int capacity,
        LaboratoryState state
) {
    public static LaboratoryJdbi fromDomain(Laboratory laboratory) {
        return new LaboratoryJdbi(
                laboratory.laboratoryId().toString(),
                laboratory.code(),
                laboratory.name(),
                laboratory.building(),
                laboratory.capacity(),
                laboratory.state()
        );
    }

    public Laboratory toDomain() {
        return new Laboratory(UUID.fromString(laboratoryId), code, name, building, capacity, state);
    }
}
