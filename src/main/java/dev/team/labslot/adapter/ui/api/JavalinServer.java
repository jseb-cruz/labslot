package dev.team.labslot.adapter.ui.api;

import dev.team.labslot.core.constant.UserRole;
import dev.team.labslot.core.entity.User;
import dev.team.labslot.core.service.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.HttpResponseException;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.json.JavalinJackson3;
import io.javalin.router.EndpointNotFound;
import io.javalin.security.BasicAuthCredentials;
import io.javalin.security.RouteRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * API REST con Javalin en el puerto 7070.
 * Es un SmartLifecycle: Spring llama a start() al arrancar y a stop() al cerrarse, así que Javalin
 * arranca y se detiene junto con la aplicación (con devtools, el puerto 7070 queda libre antes de reiniciar).
 * Usa los mismos servicios que la web (los de MariaDbConfig o InMemoryConfig, según labslot.repository), por eso ambas comparten los datos.
 */
@Component
public class JavalinServer implements SmartLifecycle {

    // Roles de las rutas de Javalin; se obtienen del rol del usuario autenticado
    public enum ApiRole implements RouteRole {
        USER,
        ADMIN
    }

    private static final int PORT = 7070;
    private static final Logger log = LoggerFactory.getLogger(JavalinServer.class);

    private final LaboratoryRestController laboratoryController;
    private final ReservationRestController reservationController;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    // JsonMapper de Jackson 3 configurado por Spring Boot (fechas como texto ISO, por ejemplo "2026-11-18")
    private final JsonMapper jsonMapper;

    private Javalin javalin;

    public JavalinServer(LaboratoryRestController laboratoryController,
                         ReservationRestController reservationController,
                         UserService userService,
                         PasswordEncoder passwordEncoder,
                         JsonMapper jsonMapper) {
        this.laboratoryController = laboratoryController;
        this.reservationController = reservationController;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void start() {
        // Javalin 7: toda la configuración y las rutas van dentro de Javalin.create(config -> ...)
        javalin = Javalin.create(config -> {
            config.startup.showJavalinBanner = false;
            // Evita el aviso "Your Javalin version is N days old" en cada arranque
            config.startup.showOldJavalinVersionWarning = false;
            config.jsonMapper(new JavalinJackson3(jsonMapper, false));

            // Autenticación HTTP Basic y control de roles antes de cada ruta que existe
            config.routes.beforeMatched(this::authenticate);

            // Errores, siempre como JSON en español
            // Error de negocio de los servicios
            config.routes.exception(IllegalArgumentException.class,
                    (exception, ctx) -> ctx.status(400).json(error(exception.getMessage())));
            // 401, 403 y 404 que lanzan la autenticación y los controladores
            config.routes.exception(HttpResponseException.class,
                    (exception, ctx) -> ctx.status(exception.getStatus()).json(error(exception.getMessage())));
            // Cuerpo que no es JSON o con valores de tipo incorrecto (por ejemplo, un estado que no existe)
            config.routes.exception(JacksonException.class,
                    (exception, ctx) -> ctx.status(400).json(error(
                            "El cuerpo de la solicitud no es un JSON válido o tiene valores con formato inválido")));
            // Cualquier otro error: 500 sin mostrar detalles internos
            config.routes.exception(Exception.class, (exception, ctx) -> {
                log.error("Error no controlado en la API", exception);
                ctx.status(500).json(error("Error interno del servidor"));
            });
            // Ruta que no existe: Javalin lanza EndpointNotFound (subclase de NotFoundResponse).
            // Se registra aparte porque Javalin usa el manejador de la clase más cercana y su mensaje viene en inglés.
            config.routes.exception(EndpointNotFound.class,
                    (exception, ctx) -> ctx.status(404).json(error("Ruta no encontrada: " + ctx.method() + " " + ctx.path())));

            // Laboratorios
            config.routes.get("/api/laboratories", laboratoryController::findAll, ApiRole.USER, ApiRole.ADMIN);
            config.routes.get("/api/laboratories/{laboratoryId}", laboratoryController::findById, ApiRole.USER, ApiRole.ADMIN);
            config.routes.post("/api/laboratories", laboratoryController::create, ApiRole.ADMIN);
            config.routes.post("/api/laboratories/{laboratoryId}/toggle-state", laboratoryController::toggleState, ApiRole.ADMIN);

            // Reservas (el administrador no reserva, igual que en la web)
            config.routes.get("/api/reservations", reservationController::findAll, ApiRole.USER, ApiRole.ADMIN);
            config.routes.get("/api/reservations/{reservationId}", reservationController::findById, ApiRole.USER, ApiRole.ADMIN);
            config.routes.post("/api/laboratories/{laboratoryId}/reservations", reservationController::create, ApiRole.USER);
            config.routes.post("/api/reservations/{reservationId}/cancel", reservationController::cancel, ApiRole.USER, ApiRole.ADMIN);
            config.routes.post("/api/reservations/{reservationId}/approve", reservationController::approve, ApiRole.ADMIN);
            config.routes.post("/api/reservations/{reservationId}/reject", reservationController::reject, ApiRole.ADMIN);
            config.routes.post("/api/reservations/{reservationId}/complete", reservationController::complete, ApiRole.ADMIN);
        }).start(PORT);
        log.info("API REST de Javalin escuchando en el puerto {}", PORT);
    }

    @Override
    public void stop() {
        if (javalin != null) {
            javalin.stop();
            javalin = null;
            log.info("API REST de Javalin detenida (puerto {} liberado)", PORT);
        }
    }

    @Override
    public boolean isRunning() {
        return javalin != null;
    }

    // Valida las credenciales HTTP Basic con los mismos usuarios y contraseñas BCrypt de la web,
    // comprueba el rol que pide la ruta y guarda el usuario en el contexto para los controladores
    private void authenticate(Context ctx) {
        Set<RouteRole> routeRoles = ctx.routeRoles();
        if (routeRoles.isEmpty()) {
            return;
        }
        BasicAuthCredentials credentials = ctx.basicAuthCredentials();
        if (credentials == null) {
            ctx.header("WWW-Authenticate", "Basic realm=\"LabSlot\"");
            throw new UnauthorizedResponse("Se requieren credenciales (HTTP Basic)");
        }
        User user = credentials.getUsername().isBlank() ? null
                : userService.findByEmail(credentials.getUsername())
                .filter(found -> passwordEncoder.matches(credentials.getPassword(), found.password()))
                .orElse(null);
        if (user == null) {
            ctx.header("WWW-Authenticate", "Basic realm=\"LabSlot\"");
            throw new UnauthorizedResponse("Correo o contraseña incorrectos");
        }
        ApiRole role = user.role() == UserRole.ROLE_ADMIN ? ApiRole.ADMIN : ApiRole.USER;
        if (!routeRoles.contains(role)) {
            throw new ForbiddenResponse("No tienes permiso para esta acción");
        }
        ctx.attribute("currentUser", user);
    }

    private static Map<String, String> error(String message) {
        return Map.of("error", Objects.requireNonNullElse(message, "Error"));
    }
}
