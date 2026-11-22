package dev.team.labslot.core.entity;

import dev.team.labslot.core.constant.Constants;
import dev.team.labslot.core.constant.LaboratoryState;

import java.util.UUID;

public record Laboratory(
        UUID laboratoryId,
        String code,
        String name,
        String building,
        int capacity,
        LaboratoryState state
) {
    public Laboratory(UUID laboratoryId, String code, String name, String building, int capacity, LaboratoryState state) {
        if (laboratoryId == null) {
            throw new IllegalArgumentException("El id del laboratorio no puede ser nulo");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El código del laboratorio no puede ser nulo o vacío");
        }
        if (code.length() > Constants.MAX_LABORATORY_CODE_LENGTH) {
            throw new IllegalArgumentException("El código del laboratorio no puede tener más de "
                    + Constants.MAX_LABORATORY_CODE_LENGTH + " caracteres");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del laboratorio no puede ser nulo o vacío");
        }
        if (name.length() > Constants.MAX_LABORATORY_NAME_LENGTH) {
            throw new IllegalArgumentException("El nombre del laboratorio no puede tener más de "
                    + Constants.MAX_LABORATORY_NAME_LENGTH + " caracteres");
        }
        if (building == null || building.isBlank()) {
            throw new IllegalArgumentException("El edificio del laboratorio no puede ser nulo o vacío");
        }
        if (building.length() > Constants.MAX_BUILDING_LENGTH) {
            throw new IllegalArgumentException("El edificio del laboratorio no puede tener más de "
                    + Constants.MAX_BUILDING_LENGTH + " caracteres");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad del laboratorio debe ser mayor que cero");
        }
        if (state == null) {
            throw new IllegalArgumentException("El estado del laboratorio no puede ser nulo");
        }
        this.laboratoryId = laboratoryId;
        this.code = code;
        this.name = name;
        this.building = building;
        this.capacity = capacity;
        this.state = state;
    }
}
