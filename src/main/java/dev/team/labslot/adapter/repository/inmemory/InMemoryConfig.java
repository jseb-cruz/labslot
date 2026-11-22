package dev.team.labslot.adapter.repository.inmemory;

import dev.team.labslot.adapter.report.OpenPdfReportGateway;
import dev.team.labslot.adapter.security.BCryptPasswordHasher;
import dev.team.labslot.core.constant.LaboratoryState;
import dev.team.labslot.core.constant.ReservationState;
import dev.team.labslot.core.constant.UserRole;
import dev.team.labslot.core.dto.LaboratoryDTO;
import dev.team.labslot.core.dto.ReservationDTO;
import dev.team.labslot.core.dto.UserDTO;
import dev.team.labslot.core.entity.Laboratory;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.port.LaboratoryRepository;
import dev.team.labslot.core.port.PasswordHasher;
import dev.team.labslot.core.port.ReportGateway;
import dev.team.labslot.core.port.ReservationRepository;
import dev.team.labslot.core.port.UserRepository;
import dev.team.labslot.core.service.LaboratoryService;
import dev.team.labslot.core.service.ReservationService;
import dev.team.labslot.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Configuración con repositorios en memoria. Crea los repositorios, el reloj, los servicios
 * y carga datos de ejemplo al arrancar la aplicación. Se activa con labslot.repository=inmemory.
 * Los tests la usan con new InMemoryConfig(), sin levantar Spring: como nunca llaman a sampleData,
 * trabajan con repositorios vacíos.
 */
@Configuration
@ConditionalOnProperty(name = "labslot.repository", havingValue = "inmemory")
public class InMemoryConfig {

    private static final Logger log = LoggerFactory.getLogger(InMemoryConfig.class);

    @Bean
    public LaboratoryRepository laboratoryRepository() {
        return new InMemoryLaboratoryRepository();
    }

    @Bean
    public ReservationRepository reservationRepository() {
        return new InMemoryReservationRepository();
    }

    @Bean
    public UserRepository userRepository() {
        return new InMemoryUserRepository();
    }

    // Fecha y hora actual en la zona horaria de Colombia
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Bogota"));
    }

    @Bean
    public LaboratoryService laboratoryService(LaboratoryRepository laboratoryRepository,
                                               ReservationRepository reservationRepository) {
        return new LaboratoryService(laboratoryRepository, reservationRepository);
    }

    // Cifrado de contraseñas con BCrypt
    @Bean
    public PasswordHasher passwordHasher() {
        return new BCryptPasswordHasher();
    }

    @Bean
    public UserService userService(UserRepository userRepository,
                                   ReservationRepository reservationRepository,
                                   PasswordHasher passwordHasher) {
        return new UserService(userRepository, reservationRepository, passwordHasher);
    }

    // Reportes PDF con OpenPDF
    @Bean
    public ReportGateway reportGateway() {
        return new OpenPdfReportGateway();
    }

    @Bean
    public ReservationService reservationService(ReservationRepository reservationRepository,
                                                 LaboratoryRepository laboratoryRepository,
                                                 UserRepository userRepository,
                                                 Clock clock,
                                                 ReportGateway reportGateway) {
        return new ReservationService(reservationRepository, laboratoryRepository, userRepository, clock, reportGateway);
    }

    /**
     * Datos de ejemplo de la USTA Tunja (los mismos de data.sql, que usa el modo mariadb). Spring ejecuta este
     * CommandLineRunner una vez al terminar de arrancar.
     * Se usan los create de los repositorios y no los de los servicios, porque el servicio rechaza
     * fechas pasadas y deja toda reserva nueva en PENDING; así se pueden cargar reservas en cualquier estado.
     * Las fechas se calculan a partir de hoy (según el Clock) para que las futuras sigan siendo futuras.
     */
    @Bean
    public CommandLineRunner sampleData(UserRepository userRepository,
                                        LaboratoryRepository laboratoryRepository,
                                        ReservationRepository reservationRepository,
                                        Clock clock) {
        return args -> {
            // Contraseñas (BCrypt): administradora = Admin2026*, los demás = Usta2026*
            String adminPassword = "$2b$10$crjW.y3h5MHRi.ABMOQuJOBkWCvB0QhgC8KdEYD4fWWfsLp5FoI9a";
            String userPassword = "$2b$10$60oukj9.UbRFbDXtPJliFORu3jxXHOQQ3wJzhUPZAx0Uydqc8lCi2";

            userRepository.create(new UserDTO("Martha Lucía Vargas Rojas", "martha.vargas@usantoto.edu.co", adminPassword, UserRole.ROLE_ADMIN));
            User jorge = userRepository.create(new UserDTO("Jorge Andrés Castro Pinzón", "jorge.castro@usantoto.edu.co", userPassword, UserRole.ROLE_USER));
            User valentina = userRepository.create(new UserDTO("Valentina Suárez Niño", "valentina.suarez@usantoto.edu.co", userPassword, UserRole.ROLE_USER));
            User daniel = userRepository.create(new UserDTO("Daniel Felipe Rincón Ávila", "daniel.rincon@usantoto.edu.co", userPassword, UserRole.ROLE_USER));

            // LAB-QUI-01 está en mantenimiento
            Laboratory sis01 = laboratoryRepository.create(new LaboratoryDTO("LAB-SIS-01", "Laboratorio de Sistemas 1", "Sede Central - Bloque A", 30, LaboratoryState.LSTATE_ACTIVE));
            Laboratory sis02 = laboratoryRepository.create(new LaboratoryDTO("LAB-SIS-02", "Laboratorio de Redes y Telemática", "Sede Central - Bloque A", 25, LaboratoryState.LSTATE_ACTIVE));
            Laboratory ele01 = laboratoryRepository.create(new LaboratoryDTO("LAB-ELE-01", "Laboratorio de Electrónica", "Sede Central - Bloque B", 20, LaboratoryState.LSTATE_ACTIVE));
            Laboratory fis01 = laboratoryRepository.create(new LaboratoryDTO("LAB-FIS-01", "Laboratorio de Física", "Sede Central - Bloque B", 24, LaboratoryState.LSTATE_ACTIVE));
            Laboratory qui01 = laboratoryRepository.create(new LaboratoryDTO("LAB-QUI-01", "Laboratorio de Química", "Sede Central - Bloque C", 18, LaboratoryState.LSTATE_MAINTENANCE));

            LocalDate today = LocalDate.now(clock);

            // Pasadas
            reservationRepository.create(sis01.laboratoryId(), jorge.userId(), new ReservationDTO(
                    today.minusDays(7), LocalTime.of(8, 0), LocalTime.of(10, 0), 28, "Clase de Programación Orientada a Objetos", ReservationState.RSTATE_COMPLETED));
            reservationRepository.create(ele01.laboratoryId(), valentina.userId(), new ReservationDTO(
                    today.minusDays(5), LocalTime.of(14, 0), LocalTime.of(16, 0), 15, "Práctica de circuitos digitales", ReservationState.RSTATE_COMPLETED));
            reservationRepository.create(qui01.laboratoryId(), jorge.userId(), new ReservationDTO(
                    today.minusDays(4), LocalTime.of(10, 0), LocalTime.of(12, 0), 16, "Práctica de química general", ReservationState.RSTATE_COMPLETED));
            reservationRepository.create(fis01.laboratoryId(), daniel.userId(), new ReservationDTO(
                    today.minusDays(3), LocalTime.of(9, 0), LocalTime.of(11, 0), 10, "Repaso grupal para el parcial de física", ReservationState.RSTATE_REJECTED));

            // Futuras. Par contiguo en LAB-SIS-01 mañana: 10:00-12:00 y 12:00-14:00
            reservationRepository.create(sis01.laboratoryId(), jorge.userId(), new ReservationDTO(
                    today.plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0), 30, "Clase de Desarrollo Orientado a Servicios", ReservationState.RSTATE_APPROVED));
            reservationRepository.create(sis01.laboratoryId(), daniel.userId(), new ReservationDTO(
                    today.plusDays(1), LocalTime.of(12, 0), LocalTime.of(14, 0), 20, "Taller de Spring Boot del semillero", ReservationState.RSTATE_PENDING));

            // CANCELLED (09:00-10:00) que se cruza con una APPROVED (08:00-11:00) en LAB-SIS-02
            reservationRepository.create(sis02.laboratoryId(), jorge.userId(), new ReservationDTO(
                    today.plusDays(2), LocalTime.of(8, 0), LocalTime.of(11, 0), 25, "Práctica de redes: configuración de VLAN", ReservationState.RSTATE_APPROVED));
            reservationRepository.create(sis02.laboratoryId(), valentina.userId(), new ReservationDTO(
                    today.plusDays(2), LocalTime.of(9, 0), LocalTime.of(10, 0), 6, "Asesoría del proyecto integrador", ReservationState.RSTATE_CANCELLED));

            reservationRepository.create(ele01.laboratoryId(), valentina.userId(), new ReservationDTO(
                    today.plusDays(3), LocalTime.of(14, 0), LocalTime.of(17, 0), 12, "Laboratorio de microcontroladores", ReservationState.RSTATE_PENDING));

            // Capacidad exacta: 24 asistentes en LAB-FIS-01 (capacidad 24)
            reservationRepository.create(fis01.laboratoryId(), daniel.userId(), new ReservationDTO(
                    today.plusDays(4), LocalTime.of(7, 0), LocalTime.of(9, 0), 24, "Práctica de mecánica: caída libre", ReservationState.RSTATE_APPROVED));

            // PENDING en el mismo horario que una REJECTED en LAB-SIS-02
            reservationRepository.create(sis02.laboratoryId(), daniel.userId(), new ReservationDTO(
                    today.plusDays(7), LocalTime.of(16, 0), LocalTime.of(18, 0), 15, "Estudio grupal de bases de datos", ReservationState.RSTATE_REJECTED));
            reservationRepository.create(sis02.laboratoryId(), valentina.userId(), new ReservationDTO(
                    today.plusDays(7), LocalTime.of(16, 0), LocalTime.of(18, 0), 10, "Ensayo de sustentación del proyecto de grado", ReservationState.RSTATE_PENDING));

            // 4 horas exactas y capacidad exacta (20) en LAB-ELE-01
            reservationRepository.create(ele01.laboratoryId(), jorge.userId(), new ReservationDTO(
                    today.plusDays(8), LocalTime.of(10, 0), LocalTime.of(14, 0), 20, "Parcial práctico de electrónica análoga", ReservationState.RSTATE_APPROVED));

            reservationRepository.create(fis01.laboratoryId(), valentina.userId(), new ReservationDTO(
                    today.plusDays(10), LocalTime.of(18, 0), LocalTime.of(20, 0), 8, "Monitoría de física mecánica", ReservationState.RSTATE_PENDING));

            log.info("Datos de ejemplo cargados: {} usuarios, {} laboratorios, {} reservas",
                    userRepository.findAll().size(),
                    laboratoryRepository.findAll().size(),
                    reservationRepository.findAll().size());
        };
    }
}
