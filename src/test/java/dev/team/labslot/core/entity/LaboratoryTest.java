package dev.team.labslot.core.entity;

import dev.team.labslot.core.constant.LaboratoryState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LaboratoryTest {

    private final UUID mockId = UUID.randomUUID();
    private final LaboratoryState mockState = LaboratoryState.LSTATE_ACTIVE;

    @Test
    void ShouldCreateLaboratoryWithValidData() {
        Laboratory laboratory = new Laboratory(
                mockId,
                "LAB-101",
                "Laboratorio de Redes",
                "Edificio Santo Domingo",
                30,
                mockState);
        assertEquals(mockId, laboratory.laboratoryId());
        assertEquals("LAB-101", laboratory.code());
        assertEquals("Laboratorio de Redes", laboratory.name());
        assertEquals("Edificio Santo Domingo", laboratory.building());
        assertEquals(30, laboratory.capacity());
        assertEquals(mockState, laboratory.state());
    }

    @Test
    void ShouldCreateLaboratoryWithCapacityOne() {
        Laboratory laboratory = new Laboratory(
                mockId,
                "LAB-101",
                "Laboratorio de Redes",
                "Edificio Santo Domingo",
                1,
                mockState);
        assertEquals(1, laboratory.capacity());
    }

    @Test
    void ShouldRejectNullId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                null,
                "LAB-101",
                "Laboratorio de Redes",
                "Edificio Santo Domingo",
                30,
                mockState));
        assertEquals("El id del laboratorio no puede ser nulo", exception.getMessage());
    }

    @Test
    void ShouldRejectNullCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                null,
                "Laboratorio de Redes",
                "Edificio Santo Domingo",
                30,
                mockState));
        assertEquals("El código del laboratorio no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectBlankCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                "   ",
                "Laboratorio de Redes",
                "Edificio Santo Domingo",
                30,
                mockState));
        assertEquals("El código del laboratorio no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectNullName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                "LAB-101",
                null,
                "Edificio Santo Domingo",
                30,
                mockState));
        assertEquals("El nombre del laboratorio no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectBlankName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                "LAB-101",
                "   ",
                "Edificio Santo Domingo",
                30,
                mockState));
        assertEquals("El nombre del laboratorio no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectNullBuilding() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                "LAB-101",
                "Laboratorio de Redes",
                null,
                30,
                mockState));
        assertEquals("El edificio del laboratorio no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectBlankBuilding() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                "LAB-101",
                "Laboratorio de Redes",
                "   ",
                30,
                mockState));
        assertEquals("El edificio del laboratorio no puede ser nulo o vacío", exception.getMessage());
    }

    @Test
    void ShouldRejectZeroCapacity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                "LAB-101",
                "Laboratorio de Redes",
                "Edificio Santo Domingo",
                0,
                mockState));
        assertEquals("La capacidad del laboratorio debe ser mayor que cero", exception.getMessage());
    }

    @Test
    void ShouldRejectNegativeCapacity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                "LAB-101",
                "Laboratorio de Redes",
                "Edificio Santo Domingo",
                -5,
                mockState));
        assertEquals("La capacidad del laboratorio debe ser mayor que cero", exception.getMessage());
    }

    @Test
    void ShouldRejectNullState() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                "LAB-101",
                "Laboratorio de Redes",
                "Edificio Santo Domingo",
                30,
                null));
        assertEquals("El estado del laboratorio no puede ser nulo", exception.getMessage());
    }

    // Caso límite: 20 caracteres exactos se permiten
    @Test
    void ShouldCreateLaboratoryWithCodeOfExactly20Characters() {
        String code = "a".repeat(20);
        Laboratory laboratory = new Laboratory(
                mockId,
                code,
                "Laboratorio de Redes",
                "Edificio Santo Domingo",
                30,
                mockState);
        assertEquals(20, laboratory.code().length());
    }

    @Test
    void ShouldRejectCodeLongerThan20Characters() {
        String code = "a".repeat(21);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                code,
                "Laboratorio de Redes",
                "Edificio Santo Domingo",
                30,
                mockState));
        assertEquals("El código del laboratorio no puede tener más de 20 caracteres", exception.getMessage());
    }

    // Caso límite: 150 caracteres exactos se permiten
    @Test
    void ShouldCreateLaboratoryWithNameOfExactly150Characters() {
        String name = "a".repeat(150);
        Laboratory laboratory = new Laboratory(
                mockId,
                "LAB-101",
                name,
                "Edificio Santo Domingo",
                30,
                mockState);
        assertEquals(150, laboratory.name().length());
    }

    @Test
    void ShouldRejectNameLongerThan150Characters() {
        String name = "a".repeat(151);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                "LAB-101",
                name,
                "Edificio Santo Domingo",
                30,
                mockState));
        assertEquals("El nombre del laboratorio no puede tener más de 150 caracteres", exception.getMessage());
    }

    // Caso límite: 150 caracteres exactos se permiten
    @Test
    void ShouldCreateLaboratoryWithBuildingOfExactly150Characters() {
        String building = "a".repeat(150);
        Laboratory laboratory = new Laboratory(
                mockId,
                "LAB-101",
                "Laboratorio de Redes",
                building,
                30,
                mockState);
        assertEquals(150, laboratory.building().length());
    }

    @Test
    void ShouldRejectBuildingLongerThan150Characters() {
        String building = "a".repeat(151);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> new Laboratory(
                mockId,
                "LAB-101",
                "Laboratorio de Redes",
                building,
                30,
                mockState));
        assertEquals("El edificio del laboratorio no puede tener más de 150 caracteres", exception.getMessage());
    }
}
