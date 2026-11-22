package dev.team.labslot.core.port;

import dev.team.labslot.core.dto.LaboratoryDTO;
import dev.team.labslot.core.entity.Laboratory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto con las operaciones de persistencia de laboratorios.
 */
public interface LaboratoryRepository {

    // Devuelve todos los laboratorios
    List<Laboratory> findAll();

    // Busca un laboratorio por su id
    Optional<Laboratory> findById(UUID laboratoryId);

    // Crea un laboratorio nuevo a partir del DTO
    Laboratory create(LaboratoryDTO laboratoryRequest);

    // Actualiza un laboratorio existente; vacío si no existe
    Optional<Laboratory> update(UUID laboratoryId, LaboratoryDTO laboratoryRequest);

    // Elimina un laboratorio por su id
    void delete(UUID laboratoryId);
}
