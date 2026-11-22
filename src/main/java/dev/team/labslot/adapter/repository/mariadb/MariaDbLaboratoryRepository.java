package dev.team.labslot.adapter.repository.mariadb;

import dev.team.labslot.adapter.repository.mariadb.entity.LaboratoryJdbi;
import dev.team.labslot.adapter.repository.mariadb.repositories.LaboratoryDao;
import dev.team.labslot.core.dto.LaboratoryDTO;
import dev.team.labslot.core.entity.Laboratory;
import dev.team.labslot.core.port.LaboratoryRepository;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de laboratorios con JDBI. Se crea con @Bean en MariaDbConfig.
 */
public class MariaDbLaboratoryRepository implements LaboratoryRepository {

    private final LaboratoryDao laboratoryDao;

    public MariaDbLaboratoryRepository(Jdbi jdbi) {
        // onDemand abre y cierra una conexión en cada llamada
        this.laboratoryDao = jdbi.onDemand(LaboratoryDao.class);
    }

    // Devuelve todos los laboratorios
    @Override
    public List<Laboratory> findAll() {
        return laboratoryDao.findAll()
                .stream()
                .map(LaboratoryJdbi::toDomain)
                .toList();
    }

    // Busca un laboratorio por su id
    @Override
    public Optional<Laboratory> findById(UUID laboratoryId) {
        return laboratoryDao.findById(laboratoryId.toString())
                .map(LaboratoryJdbi::toDomain);
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
        laboratoryDao.insert(LaboratoryJdbi.fromDomain(laboratory));
        return laboratory;
    }

    // Actualiza un laboratorio existente conservando su id; vacío si no existe
    @Override
    public Optional<Laboratory> update(UUID laboratoryId, LaboratoryDTO laboratoryRequest) {
        return findById(laboratoryId)
                .map(existing -> {
                    Laboratory updatedLaboratory = new Laboratory(
                            existing.laboratoryId(),
                            laboratoryRequest.code(),
                            laboratoryRequest.name(),
                            laboratoryRequest.building(),
                            laboratoryRequest.capacity(),
                            laboratoryRequest.state()
                    );
                    laboratoryDao.update(LaboratoryJdbi.fromDomain(updatedLaboratory));
                    return updatedLaboratory;
                });
    }

    // Elimina un laboratorio por su id
    @Override
    public void delete(UUID laboratoryId) {
        laboratoryDao.delete(laboratoryId.toString());
    }
}
