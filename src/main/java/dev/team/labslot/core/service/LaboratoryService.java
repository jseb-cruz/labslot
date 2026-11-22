package dev.team.labslot.core.service;

import dev.team.labslot.core.dto.LaboratoryDTO;
import dev.team.labslot.core.entity.Laboratory;
import dev.team.labslot.core.port.LaboratoryRepository;
import dev.team.labslot.core.port.ReservationRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LaboratoryService {
    private final LaboratoryRepository laboratoryRepository;
    // Se usa para no eliminar laboratorios que tengan reservas
    private final ReservationRepository reservationRepository;

    public LaboratoryService(LaboratoryRepository laboratoryRepository, ReservationRepository reservationRepository) {
        this.laboratoryRepository = laboratoryRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Laboratory> findAll() {
        return laboratoryRepository.findAll();
    }

    public Optional<Laboratory> findById(UUID laboratoryId) {
        if (laboratoryId == null) {
            throw new IllegalArgumentException("El id del laboratorio no puede ser nulo");
        }
        return laboratoryRepository.findById(laboratoryId);
    }

    public Laboratory create(LaboratoryDTO laboratoryRequest) {
        if (laboratoryRequest == null) {
            throw new IllegalArgumentException("La solicitud de laboratorio no puede ser nula");
        }
        // El código es único sin distinguir mayúsculas: "lab-101" se considera igual a "LAB-101"
        boolean codeAlreadyExists = laboratoryRepository.findAll().stream()
                .anyMatch(laboratory -> laboratory.code().equalsIgnoreCase(laboratoryRequest.code()));
        if (codeAlreadyExists) {
            throw new IllegalArgumentException("Ya existe un laboratorio con ese código");
        }
        return laboratoryRepository.create(laboratoryRequest);
    }

    public Optional<Laboratory> update(UUID laboratoryId, LaboratoryDTO laboratoryRequest) {
        if (laboratoryId == null) {
            throw new IllegalArgumentException("El id del laboratorio no puede ser nulo");
        }
        if (laboratoryRequest == null) {
            throw new IllegalArgumentException("La solicitud de laboratorio no puede ser nula");
        }
        if (laboratoryRepository.findById(laboratoryId).isEmpty()) {
            throw new IllegalArgumentException("No existe el laboratorio a actualizar");
        }
        // Se excluye el propio laboratorio para que pueda conservar su código
        boolean codeAlreadyExists = laboratoryRepository.findAll().stream()
                .anyMatch(laboratory -> !laboratory.laboratoryId().equals(laboratoryId)
                        && laboratory.code().equalsIgnoreCase(laboratoryRequest.code()));
        if (codeAlreadyExists) {
            throw new IllegalArgumentException("Ya existe otro laboratorio con ese código");
        }
        return laboratoryRepository.update(laboratoryId, laboratoryRequest);
    }

    public void delete(UUID laboratoryId) {
        if (laboratoryId == null) {
            throw new IllegalArgumentException("El id del laboratorio no puede ser nulo");
        }
        if (laboratoryRepository.findById(laboratoryId).isEmpty()) {
            throw new IllegalArgumentException("No existe el laboratorio a eliminar");
        }
        // Cuenta cualquier reserva, sin importar su estado, para no dejar reservas huérfanas
        boolean hasReservations = reservationRepository.findAll().stream()
                .anyMatch(reservation -> reservation.laboratoryId().equals(laboratoryId));
        if (hasReservations) {
            throw new IllegalArgumentException("No se puede eliminar un laboratorio con reservas");
        }
        laboratoryRepository.delete(laboratoryId);
    }
}
