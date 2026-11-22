package dev.team.labslot.core.dto;

import dev.team.labslot.core.constant.LaboratoryState;

/**
 * DTO con los datos de un laboratorio (sin id).
 */
public record LaboratoryDTO(
        String code,
        String name,
        String building,
        int capacity,
        LaboratoryState state
) {
}
