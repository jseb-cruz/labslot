package dev.team.labslot.adapter.repository.inmemory;

import dev.team.labslot.adapter.report.OpenPdfReportGateway;
import dev.team.labslot.adapter.security.BCryptPasswordHasher;
import dev.team.labslot.core.constant.UserRole;
import dev.team.labslot.core.dto.UserDTO;
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
import java.time.ZoneId;

/**
 * Configuración con repositorios en memoria. Crea los repositorios, el reloj, los servicios
 * y crea el administrador general al arrancar la aplicación. Se activa con labslot.repository=inmemory.
 * Los tests la usan con new InMemoryConfig(), sin levantar Spring: como nunca llaman a defaultAdministrator,
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
     * Crea el administrador general (el mismo de data.sql, que usa el modo mariadb). Spring ejecuta este
     * CommandLineRunner una vez al terminar de arrancar. Los demás usuarios se crean desde la web.
     * Se usa el create del repositorio y no el del servicio porque la contraseña ya viene cifrada.
     */
    @Bean
    public CommandLineRunner defaultAdministrator(UserRepository userRepository) {
        return args -> {
            // Contraseña (BCrypt): Admin2026*. Es conocida: en un uso real hay que cambiarla
            String password = "$2b$10$crjW.y3h5MHRi.ABMOQuJOBkWCvB0QhgC8KdEYD4fWWfsLp5FoI9a";

            User administrator = userRepository.create(new UserDTO("Administrador General", "admin@usantoto.edu.co", password, UserRole.ROLE_ADMIN));
            log.info("Administrador general creado: {}", administrator.email());
        };
    }
}
