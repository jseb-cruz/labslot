package dev.team.labslot.adapter.repository.mariadb;

import dev.team.labslot.adapter.report.OpenPdfReportGateway;
import dev.team.labslot.adapter.security.BCryptPasswordHasher;
import dev.team.labslot.core.port.LaboratoryRepository;
import dev.team.labslot.core.port.PasswordHasher;
import dev.team.labslot.core.port.ReportGateway;
import dev.team.labslot.core.port.ReservationRepository;
import dev.team.labslot.core.port.UserRepository;
import dev.team.labslot.core.service.LaboratoryService;
import dev.team.labslot.core.service.ReservationService;
import dev.team.labslot.core.service.UserService;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.Clock;
import java.time.ZoneId;

/**
 * Configuración con repositorios JDBI sobre MariaDB. Crea Jdbi, los repositorios, el reloj y los servicios.
 * Se activa con labslot.repository=mariadb, o si la propiedad no está definida.
 */
@Configuration
@ConditionalOnProperty(name = "labslot.repository", havingValue = "mariadb", matchIfMissing = true)
public class MariaDbConfig {

    // Se usa el DataSource de Spring para trabajar sobre la misma base donde corren schema.sql y data.sql
    @Bean
    public Jdbi jdbi(DataSource dataSource) {
        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }

    @Bean
    public LaboratoryRepository laboratoryRepository(Jdbi jdbi) {
        return new MariaDbLaboratoryRepository(jdbi);
    }

    @Bean
    public ReservationRepository reservationRepository(Jdbi jdbi) {
        return new MariaDbReservationRepository(jdbi);
    }

    @Bean
    public UserRepository userRepository(Jdbi jdbi) {
        return new MariaDbUserRepository(jdbi);
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
}
