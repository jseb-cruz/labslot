package dev.team.labslot.adapter.repository.inmemory;

import dev.team.labslot.core.dto.LaboratoryDTO;
import dev.team.labslot.core.entity.Laboratory;
import dev.team.labslot.core.port.LaboratoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de laboratorios en memoria. Guarda los laboratorios en una lista interna.
 */
public class InMemoryLaboratoryRepository implements LaboratoryRepository {

    // Lista interna de almacenamiento
    private final ArrayList<Laboratory> laboratories = new ArrayList<>();

    // Devuelve una copia inmutable para que nadie modifique la lista interna desde afuera
    @Override
    public List<Laboratory> findAll() {
        return List.copyOf(laboratories);
    }

    // Busca un laboratorio por su id
    @Override
    public Optional<Laboratory> findById(UUID laboratoryId) {
        return laboratories.stream()
                .filter(laboratory -> laboratory.laboratoryId().equals(laboratoryId))
                .findFirst();
    }

    // Crea un laboratorio con un id nuevo
    @Override
    public Laboratory create(LaboratoryDTO laboratoryRequest) {
        Laboratory laboratory = new Laboratory(
                UUID.randomUUID(),
                laboratoryRequest.code(),
                laboratoryRequest.name(),
                laboratoryRequest.building(),
                laboratoryRequest.capacity(),
                laboratoryRequest.state()
        );
        laboratories.add(laboratory);
        return laboratory;
    }

    // Actualiza un laboratorio existente conservando su id; vacío si no existe
    @Override
    public Optional<Laboratory> update(UUID laboratoryId, LaboratoryDTO laboratoryRequest) {
        for (Laboratory laboratory : laboratories) {
            if (laboratory.laboratoryId().equals(laboratoryId)) {
                Laboratory updatedLaboratory = new Laboratory(
                        laboratory.laboratoryId(),
                        laboratoryRequest.code(),
                        laboratoryRequest.name(),
                        laboratoryRequest.building(),
                        laboratoryRequest.capacity(),
                        laboratoryRequest.state()
                );
                laboratories.set(laboratories.indexOf(laboratory), updatedLaboratory);
                return Optional.of(updatedLaboratory);
            }
        }
        return Optional.empty();
    }

    // Elimina un laboratorio por su id
    @Override
    public void delete(UUID laboratoryId) {
        laboratories.removeIf(l -> l.laboratoryId().equals(laboratoryId));
    }
}
