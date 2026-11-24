# LabSlot

<div align="center">

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-9.9.1-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)](https://mariadb.org/)
[![JDBI](https://img.shields.io/badge/JDBI-3.55.0-4B6A88?style=for-the-badge)](https://jdbi.org/)
[![Javalin](https://img.shields.io/badge/Javalin-7.2.2-3B7CBF?style=for-the-badge)](https://javalin.io/)
[![JUnit](https://img.shields.io/badge/JUnit-6-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/)

</div>

Proyecto integrador de **Desarrollo Orientado a Servicios 2026-2**, Universidad Santo Tomás, Seccional Tunja.

## Descripción del proyecto

1. **Problema:** los laboratorios de la universidad se reservan por correo o en papel, y dos grupos terminan con el mismo laboratorio a la misma hora.
2. **A quién le sirve:** a los estudiantes y docentes que solicitan reservas, y al administrador que gestiona los laboratorios y aprueba o rechaza las solicitudes.
3. **Qué datos guarda y consulta:** los laboratorios, las reservas por franjas horarias y los usuarios con su rol.

### Funcionalidades

- Solicitud de reservas por franja horaria, sin cruces de horario.
- Aprobación, rechazo, cancelación y finalización de reservas.
- Gestión de laboratorios: listar, crear y alternar entre activo y mantenimiento.
- Gestión de usuarios para el administrador: listar, crear y eliminar.
- Inicio de sesión con dos roles: administrador y usuario.
- Páginas de error propias (403, 404 y 500).
- API REST en JSON con los mismos usuarios y las mismas reglas de la web.
- Reporte de reservas en PDF, descargable desde la web.
- Dos formas de guardar los datos: MariaDB o memoria.

### Dominio y reglas de negocio

#### Entidades y enums

| Entidad | Datos |
|---|---|
| `Laboratory` | `laboratoryId`, `code`, `name`, `building`, `capacity`, `state` |
| `Reservation` | `reservationId`, `laboratoryId`, `userId`, `date`, `startTime`, `endTime`, `attendees`, `purpose`, `state`, `createdAt` |
| `User` | `userId`, `fullName`, `email`, `password`, `role` |

- `LaboratoryState`: `LSTATE_ACTIVE`, `LSTATE_MAINTENANCE`
- `ReservationState`: `RSTATE_PENDING`, `RSTATE_APPROVED`, `RSTATE_REJECTED`, `RSTATE_CANCELLED`, `RSTATE_COMPLETED`
- `UserRole`: `ROLE_ADMIN`, `ROLE_USER`

#### Reglas de las reservas

- El laboratorio y el usuario deben existir, y el laboratorio debe estar activo.
- Los asistentes deben ser más de cero y no superar la capacidad del laboratorio. Llenarlo exactamente sí se permite.
- El propósito es obligatorio y tiene máximo 255 caracteres.
- La hora de fin debe ser posterior a la de inicio.
- La franja debe estar entre las 06:00 y las 22:00, ambas incluidas.
- La duración máxima es de 4 horas. Exactamente 4 horas sí se permite.
- No se reserva en una fecha pasada, ni hoy en una hora que ya pasó (hora de Colombia).
- No puede cruzarse con otra reserva pendiente o aprobada del mismo laboratorio el mismo día. Dos reservas seguidas (10:00–12:00 y 12:00–14:00) no se cruzan.
- Toda reserva nueva queda pendiente.
- Un usuario solo cancela sus propias reservas; el administrador cancela cualquiera.

Además, el código de un laboratorio es único, sin distinguir mayúsculas.

#### Reglas de los usuarios

- El correo es único, sin distinguir mayúsculas, y debe tener un formato válido.
- El nombre y el correo tienen máximo 150 caracteres.
- La contraseña tiene mínimo 8 caracteres y máximo 72 (las tildes y la ñ cuentan doble, por el límite de BCrypt).
- La contraseña se guarda como hash BCrypt, nunca en texto plano.
- No se puede eliminar un usuario con reservas, en cualquier estado.
- Un administrador no puede eliminar su propio usuario, así siempre queda al menos uno.

#### Cambios de estado

| Acción | Desde | Queda en |
|---|---|---|
| Aprobar | Pendiente | Aprobada |
| Rechazar | Pendiente | Rechazada |
| Cancelar | Pendiente o aprobada | Cancelada |
| Completar | Aprobada | Completada |

### Usuarios y roles

La aplicación empieza con un solo usuario:

| Nombre | Correo | Contraseña | Rol |
|---|---|---|---|
| Administrador General | `admin@usantoto.edu.co` | `Admin2026*` | Administrador |

Los demás usuarios los crea el administrador desde la web, en **Usuarios** (<http://localhost:8080/admin/users>), donde también puede eliminar los que no tengan reservas. La lista muestra nombre, correo y rol, nunca la contraseña. Si se elimina a un usuario con la sesión abierta, su siguiente petición lo lleva al inicio de sesión. Las contraseñas no se guardan en texto plano, sino como hashes BCrypt.

> **Importante:** la contraseña del administrador general es conocida, porque está publicada aquí. En un uso real hay que reemplazarlo. La aplicación no tiene una pantalla para cambiar contraseñas, así que, en modo `mariadb`, se hace de esta forma:
>
> 1. Entra con el administrador general y crea en **Usuarios** otro administrador, con una contraseña propia.
> 2. Entra con el nuevo administrador y elimina al administrador general.
> 3. Cambia `spring.sql.init.mode` de `always` a `never` en `application.properties` (la primera de las dos líneas). Si no, `data.sql` vuelve a crear al administrador general en el siguiente arranque.

| Acción | Usuario | Administrador |
|---|---|---|
| Ver reservas | Web y API, solo las suyas | Web y API, todas, con filtro por estado |
| Filtrar sus reservas por estado | API | – |
| Crear reservas | Web y API | No |
| Cancelar reservas | Web y API, solo las suyas | Web y API, cualquiera |
| Aprobar, rechazar y completar | No | Web y API |
| Consultar laboratorios | API; en la web, los activos al crear una reserva | Web y API |
| Crear laboratorios y cambiar su estado | No | Web y API |
| Ver, crear y eliminar usuarios | No | Web |
| Descargar el reporte PDF | No | Web |

### Integrantes

- Juan Sebastian Cruz Gallo
- Luis Felipe Arias Soto
- Miller Barrera
- Juan Esteban Leon

### Licencia

Este proyecto está en desarrollo y todavía no declara una licencia pública.

## Pasos para correr el proyecto

### Inicio rápido

1. Instala **JDK 17** y **MariaDB**, y deja MariaDB corriendo ([requisitos](#requisitos)).
2. Crea la base y el usuario `labslot` ([preparar la base de datos](#preparar-la-base-de-datos)).
3. Clona el proyecto y entra en su carpeta:
   ```bash
   git clone https://github.com/jseb-cruz/labslot.git
   cd labslot
   ```
4. Define la contraseña de la base y arranca la aplicación:
   ```bash
   export DB_PASSWORD='TuContraseña'
   ./gradlew bootRun
   ```
5. Abre <http://localhost:8080> y entra como administrador: `admin@usantoto.edu.co` / `Admin2026*`. Crea un laboratorio en **Laboratorios** y un usuario en **Usuarios**; con ese usuario ya se pueden hacer reservas ([usuarios y roles](#usuarios-y-roles)).

**¿Sin MariaDB?** Salta los pasos de la base y arranca en modo memoria: `LABSLOT_REPOSITORY=inmemory ./gradlew bootRun`

**¿Necesitas datos para una demostración?** Mira [datos de demostración](#datos-de-demostración).

### Requisitos

- **JDK 17.** Debe estar instalado aunque uses otra versión por defecto: Gradle lo busca para compilar y ejecutar, y el proyecto no lo descarga.
- **MariaDB** en `localhost:3306`. No hace falta en modo `inmemory`.
- Gradle no se instala: se usa el wrapper `./gradlew` incluido.

Arch / EndeavourOS:

```bash
sudo pacman -S jdk17-openjdk mariadb
sudo mariadb-install-db --user=mysql --basedir=/usr --datadir=/var/lib/mysql
sudo systemctl start mariadb
```

Ubuntu / Debian (el paquete `openjdk-17-jdk` depende de la versión del sistema):

```bash
sudo apt update
sudo apt install openjdk-17-jdk mariadb-server
sudo systemctl start mariadb
```

`systemctl start` inicia MariaDB solo hasta que apagues el equipo. Para que arranque siempre con el sistema: `sudo systemctl enable mariadb`.

### Clonar

```bash
git clone https://github.com/jseb-cruz/labslot.git
cd labslot
```

### Preparar la base de datos

Entra en MariaDB como administrador:

```bash
sudo mariadb
```

Crea la base y el usuario, cambiando `TuContraseña` por la tuya:

```sql
CREATE DATABASE labslot CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'labslot'@'localhost' IDENTIFIED BY 'TuContraseña';
GRANT ALL PRIVILEGES ON labslot.* TO 'labslot'@'localhost';
EXIT;
```

Con `EXIT;` se sale de MariaDB y se vuelve a la terminal.

Las tablas y el administrador general **se crean solos** al arrancar la aplicación: `schema.sql` crea las tablas que falten y `data.sql` inserta el administrador. Los laboratorios y las reservas empiezan vacíos. Los arranques siguientes no borran ni duplican nada.

### Configurar la contraseña

La contraseña no está en el proyecto: se lee de la variable de entorno `DB_PASSWORD`.

**En la terminal** (dura hasta cerrar esa terminal):

```bash
export DB_PASSWORD='TuContraseña'
```

Para que no quede en el historial de la terminal, se puede escribir sin mostrarla (bash o zsh):

```bash
read -rs DB_PASSWORD && export DB_PASSWORD
```

**En IntelliJ IDEA:**

1. *Run → Edit Configurations* y elige la configuración de `Application`.
2. *Modify options → Environment variables* y escribe `DB_PASSWORD=TuContraseña`.
3. No marques *Store as project file*, para que la contraseña no termine en el repositorio.

### Ejecutar

Desde la terminal:

```bash
./gradlew bootRun
```

Desde IntelliJ IDEA, ejecuta la clase `dev.team.labslot.Application`.

| Servicio | Dirección |
|---|---|
| Web | <http://localhost:8080> |
| API REST | <http://localhost:7070/api> |

### Modos de almacenamiento

La propiedad `labslot.repository` elige dónde se guardan los datos:

| Modo | Para qué sirve | Datos |
|---|---|---|
| `mariadb` (por defecto) | Uso normal: los datos se conservan al reiniciar | `schema.sql` y `data.sql` |
| `inmemory` | Probar o mostrar el proyecto sin base de datos | Solo el administrador general, creado en cada arranque; todo se pierde al cerrar |

Para usar el modo `inmemory` sin cambiar archivos:

```bash
LABSLOT_REPOSITORY=inmemory ./gradlew bootRun
```

En IntelliJ IDEA, agrega `LABSLOT_REPOSITORY=inmemory` a las variables de entorno. En este modo no hacen falta MariaDB ni `DB_PASSWORD`.

### Errores comunes al arrancar

Si algo falla con la base, la aplicación se detiene al arrancar. Busca estas líneas `Caused by` al final del error:

| Mensaje | Causa | Solución |
|---|---|---|
| `Socket fail to connect to localhost:3306` y `Conexión rehusada` (o `Connection refused`) | MariaDB está apagada | `sudo systemctl start mariadb` |
| `Access denied for user 'labslot'@'localhost' (using password: NO)` | `DB_PASSWORD` no está definida | Definirla en la misma terminal o en la configuración de IntelliJ |
| `Access denied for user 'labslot'@'localhost' (using password: YES)` | La contraseña es incorrecta o el usuario `labslot` no existe | Corregir `DB_PASSWORD` o revisar [la preparación de la base](#preparar-la-base-de-datos) |

### Datos de demostración

`src/main/resources/demo-data.sql` tiene 4 usuarios, 5 laboratorios y 14 reservas en distintos estados. **No se carga solo** y sirve únicamente en modo `mariadb`.

Arranca la aplicación al menos una vez, para que existan las tablas, y desde la carpeta del proyecto ejecuta:

```bash
mariadb -h localhost -u labslot -p --default-character-set=utf8mb4 labslot < src/main/resources/demo-data.sql
```

Pide la contraseña del usuario `labslot`. Cargarlo dos veces no duplica datos. Estos son sus usuarios:

| Nombre | Correo | Contraseña | Rol |
|---|---|---|---|
| Martha Lucía Vargas Rojas | `martha.vargas@usantoto.edu.co` | `Admin2026*` | Administrador |
| Jorge Andrés Castro Pinzón | `jorge.castro@usantoto.edu.co` | `Usta2026*` | Usuario |
| Valentina Suárez Niño | `valentina.suarez@usantoto.edu.co` | `Usta2026*` | Usuario |
| Daniel Felipe Rincón Ávila | `daniel.rincon@usantoto.edu.co` | `Usta2026*` | Usuario |

Las fechas de las reservas se calculan **el día en que se carga** y después no se mueven, así que conviene cargarlo el mismo día de la demostración. Para recalcularlas, vacía las reservas y vuelve a cargarlo:

```bash
mariadb -h localhost -u labslot -p labslot -e "DELETE FROM reservations;"
```

Para volver a empezar solo con el administrador general, detén la aplicación, vacía las tres tablas y arráncala de nuevo:

```bash
mariadb -h localhost -u labslot -p labslot -e "DELETE FROM reservations; DELETE FROM laboratories; DELETE FROM users;"
```

> **Advertencia:** estos dos comandos no se pueden deshacer y borran también los datos que hayas creado tú en esas tablas.

### Tests

```bash
./gradlew test
```

Son **117 tests** con JUnit 6 (Jupiter), que no necesitan MariaDB:

| Clase | Tests | Qué cubre |
|---|---|---|
| `LaboratoryTest` | 18 | Datos obligatorios, capacidad y longitudes |
| `ReservationTest` | 24 | Datos obligatorios, horario 06:00–22:00, máximo 4 horas y longitud del propósito, con sus casos límite |
| `UserTest` | 16 | Datos obligatorios, formato del correo y longitudes |
| `ServiceBusinessRulesTest` | 59 | Reglas de los servicios: unicidad, cruces, capacidad, fechas, cambios de estado, permisos para cancelar, contraseñas, eliminación de usuarios y reporte, con los repositorios en memoria |

La web, la API, los repositorios de MariaDB, el cifrado con BCrypt y el diseño del PDF no tienen tests automáticos.

### Comandos útiles

```bash
./gradlew clean
./gradlew test
./gradlew build
./gradlew bootRun
```

## Estructura de carpetas

```text
labslot/
├── src/
│   ├── main/
│   │   ├── java/dev/team/labslot/
│   │   │   ├── Application.java
│   │   │   ├── core/
│   │   │   │   ├── constant/      Constants, LaboratoryState, ReservationState, UserRole
│   │   │   │   ├── dto/           LaboratoryDTO, ReservationDTO, UserDTO
│   │   │   │   ├── entity/        Laboratory, Reservation, User
│   │   │   │   ├── port/          LaboratoryRepository, ReservationRepository, UserRepository,
│   │   │   │   │                  ReportGateway, PasswordHasher
│   │   │   │   └── service/       LaboratoryService, ReservationService, UserService
│   │   │   └── adapter/
│   │   │       ├── report/        OpenPdfReportGateway
│   │   │       ├── security/      BCryptPasswordHasher
│   │   │       ├── repository/
│   │   │       │   ├── inmemory/  InMemoryConfig, InMemoryLaboratoryRepository,
│   │   │       │   │              InMemoryReservationRepository, InMemoryUserRepository
│   │   │       │   └── mariadb/   MariaDbConfig, MariaDbLaboratoryRepository,
│   │   │       │       │          MariaDbReservationRepository, MariaDbUserRepository
│   │   │       │       ├── entity/        LaboratoryJdbi, ReservationJdbi, UserJdbi
│   │   │       │       └── repositories/  LaboratoryDao, ReservationDao, UserDao
│   │   │       └── ui/
│   │   │           ├── api/       JavalinServer, LaboratoryRestController, ReservationRestController
│   │   │           └── web/       HomeController, ReservationController, AdminReservationController,
│   │   │                          LaboratoryController, UserController, SecurityConfig,
│   │   │                          WebControllerAdvice
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema.sql
│   │       ├── data.sql               administrador general (se carga solo)
│   │       ├── demo-data.sql          datos de demostración (se carga a mano)
│   │       ├── static/            Bootstrap (CSS y JavaScript)
│   │       └── templates/         vistas Thymeleaf y páginas de error
│   └── test/java/dev/team/labslot/core/
│       ├── entity/                LaboratoryTest, ReservationTest, UserTest
│       └── service/               ServiceBusinessRulesTest
├── docs/capturas/                  capturas de pantalla del README
├── gradle/wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── .gitattributes
├── .gitignore
└── README.md
```

### Qué hay en cada capa

| Capa | Paquete | Responsabilidad |
|---|---|---|
| Dominio | `core/entity`, `core/constant` | Entidades que validan sus datos al construirse, estados, roles y límites |
| Datos de entrada | `core/dto` | Datos para crear o actualizar, sin ids |
| Puertos | `core/port` | Interfaces de lo que el dominio necesita del exterior: guardar datos, generar reportes y cifrar contraseñas |
| Servicios | `core/service` | Casos de uso y reglas de negocio |
| Adaptadores de entrada | `adapter/ui/web`, `adapter/ui/api` | Reciben las peticiones de la web y de la API, y llaman a los servicios |
| Adaptadores de salida | `adapter/repository/*`, `adapter/report`, `adapter/security` | Implementan los puertos: memoria, MariaDB, PDF y BCrypt |
| Configuración | `InMemoryConfig`, `MariaDbConfig` | Crean los repositorios y los servicios; `labslot.repository` elige cuál se usa |

## Diagrama de arquitectura

El proyecto sigue la **arquitectura hexagonal**: el dominio (`core`) define interfaces (puertos) y los adaptadores las implementan. Los adaptadores de entrada (web y API) llaman a los servicios; los servicios solo conocen los puertos.

### Diagrama de clases

Dominio: entidades, puertos y servicios. Las flechas punteadas con triángulo son "implementa"; las continuas, "usa".

```mermaid
classDiagram
    direction LR

    class Laboratory {
        <<record>>
        UUID laboratoryId
        String code
        String name
        String building
        int capacity
        LaboratoryState state
    }
    class Reservation {
        <<record>>
        UUID reservationId
        UUID laboratoryId
        UUID userId
        LocalDate date
        LocalTime startTime
        LocalTime endTime
        int attendees
        String purpose
        ReservationState state
        LocalDateTime createdAt
    }
    class User {
        <<record>>
        UUID userId
        String fullName
        String email
        String password
        UserRole role
    }

    class LaboratoryRepository {
        <<interface>>
        +findAll() List~Laboratory~
        +findById(UUID) Optional~Laboratory~
        +create(LaboratoryDTO) Laboratory
        +update(UUID, LaboratoryDTO) Optional~Laboratory~
        +delete(UUID)
    }
    class ReservationRepository {
        <<interface>>
        +findAll() List~Reservation~
        +findById(UUID) Optional~Reservation~
        +create(UUID, UUID, ReservationDTO) Reservation
        +update(UUID, ReservationDTO) Optional~Reservation~
        +delete(UUID)
    }
    class UserRepository {
        <<interface>>
        +findAll() List~User~
        +findById(UUID) Optional~User~
        +create(UserDTO) User
        +update(UUID, UserDTO) Optional~User~
        +delete(UUID)
    }
    class ReportGateway {
        <<interface>>
        +generateReservationReport(...) byte[]
    }
    class PasswordHasher {
        <<interface>>
        +hash(String) String
    }

    class LaboratoryService {
        +findAll() List~Laboratory~
        +create(LaboratoryDTO) Laboratory
        +update(UUID, LaboratoryDTO) Optional~Laboratory~
        +delete(UUID)
    }
    class ReservationService {
        +findAll() List~Reservation~
        +findByUser(UUID) List~Reservation~
        +create(UUID, UUID, ReservationDTO) Reservation
        +approve(UUID) Reservation
        +reject(UUID) Reservation
        +cancel(UUID, UUID) Reservation
        +complete(UUID) Reservation
        +generateReport(LocalDate, LocalDate) byte[]
    }
    class UserService {
        +findAll() List~User~
        +findByEmail(String) Optional~User~
        +create(UserDTO) User
        +update(UUID, UserDTO) Optional~User~
        +delete(UUID, UUID)
    }

    LaboratoryService --> LaboratoryRepository : usa
    LaboratoryService --> ReservationRepository : usa
    UserService --> UserRepository : usa
    UserService --> ReservationRepository : usa
    UserService --> PasswordHasher : usa
    ReservationService --> ReservationRepository : usa
    ReservationService --> LaboratoryRepository : usa
    ReservationService --> UserRepository : usa
    ReservationService --> ReportGateway : usa

    LaboratoryRepository ..> Laboratory
    ReservationRepository ..> Reservation
    UserRepository ..> User
```

Adaptadores de salida: implementan los puertos. Los de MariaDB delegan el SQL en los DAO de JDBI, que leen y escriben los records `XJdbi`.

```mermaid
classDiagram
    direction LR

    class LaboratoryRepository {
        <<interface>>
    }
    class ReservationRepository {
        <<interface>>
    }
    class UserRepository {
        <<interface>>
    }
    class ReportGateway {
        <<interface>>
    }
    class PasswordHasher {
        <<interface>>
    }

    InMemoryLaboratoryRepository ..|> LaboratoryRepository
    MariaDbLaboratoryRepository ..|> LaboratoryRepository
    InMemoryReservationRepository ..|> ReservationRepository
    MariaDbReservationRepository ..|> ReservationRepository
    InMemoryUserRepository ..|> UserRepository
    MariaDbUserRepository ..|> UserRepository
    OpenPdfReportGateway ..|> ReportGateway
    BCryptPasswordHasher ..|> PasswordHasher

    LaboratoryDao <-- MariaDbLaboratoryRepository : usa
    ReservationDao <-- MariaDbReservationRepository : usa
    UserDao <-- MariaDbUserRepository : usa
    LaboratoryJdbi <.. LaboratoryDao
    ReservationJdbi <.. ReservationDao
    UserJdbi <.. UserDao
```

Adaptadores de entrada: los controladores web y la API solo llaman a los servicios.

```mermaid
classDiagram
    direction LR

    class LaboratoryService
    class ReservationService
    class UserService

    ReservationController --> ReservationService : usa
    ReservationController --> LaboratoryService : usa
    AdminReservationController --> ReservationService : usa
    AdminReservationController --> LaboratoryService : usa
    AdminReservationController --> UserService : usa
    LaboratoryController --> LaboratoryService : usa
    UserController --> UserService : usa
    SecurityConfig --> UserService : usa
    WebControllerAdvice --> UserService : usa
    LaboratoryRestController --> LaboratoryService : usa
    ReservationRestController --> ReservationService : usa
    ReservationRestController --> LaboratoryService : usa
    JavalinServer --> LaboratoryRestController : usa
    JavalinServer --> ReservationRestController : usa
    JavalinServer --> UserService : usa
```

Quién crea cada objeto: `InMemoryConfig` o `MariaDbConfig` (según `labslot.repository`) crea con `@Bean` los tres repositorios, `OpenPdfReportGateway`, `BCryptPasswordHasher` y los tres servicios, y Spring se los inyecta a los controladores por el constructor.

### Diagrama de flujo

Cómo llegan la web y la API a los servicios, y cómo los servicios usan los adaptadores a través de los puertos.

```mermaid
flowchart LR
    browser([Navegador]) -->|"puerto 8080"| web
    client([Cliente HTTP]) -->|"puerto 7070"| api

    subgraph entrada["Adaptadores de entrada"]
        web["ui/web<br/>Spring MVC + Thymeleaf"]
        api["ui/api<br/>Javalin"]
    end

    subgraph core["core"]
        services["service<br/>LaboratoryService<br/>ReservationService<br/>UserService"]
        repoPorts["port<br/>LaboratoryRepository<br/>ReservationRepository<br/>UserRepository"]
        reportPort["port<br/>ReportGateway"]
        hasherPort["port<br/>PasswordHasher"]
        services --> repoPorts
        services --> reportPort
        services --> hasherPort
    end

    subgraph salida["Adaptadores de salida"]
        memory["repository/inmemory"]
        mariadb["repository/mariadb<br/>JDBI"]
        pdf["report<br/>OpenPDF"]
        bcrypt["security<br/>BCrypt"]
    end

    web --> services
    api --> services
    memory -.->|implementa| repoPorts
    mariadb -.->|implementa| repoPorts
    pdf -.->|implementa| reportPort
    bcrypt -.->|implementa| hasherPort
    mariadb --> db[(MariaDB)]
```

### Regla de dependencias

Las dependencias van siempre **de `adapter` hacia `core`**. El paquete `core` no importa nada de `adapter`, de Spring, de Javalin, de JDBI ni de OpenPDF: solo Java. Por eso se puede cambiar MariaDB por memoria, o agregar la API, sin modificar las reglas de negocio.

## Modelo Entidad/Relación

Las tablas las crea `schema.sql` en MariaDB. Un usuario hace muchas reservas y un laboratorio recibe muchas reservas; cada reserva pertenece a un usuario y a un laboratorio.

```mermaid
erDiagram
    users ||--o{ reservations : "hace"
    laboratories ||--o{ reservations : "recibe"

    users {
        varchar user_id PK "VARCHAR(36), UUID"
        varchar full_name "VARCHAR(150)"
        varchar email UK "VARCHAR(150), uk_users_email"
        varchar password "VARCHAR(100), hash BCrypt"
        varchar role "VARCHAR(30): ROLE_ADMIN o ROLE_USER"
    }
    laboratories {
        varchar laboratory_id PK "VARCHAR(36), UUID"
        varchar code UK "VARCHAR(20), uk_laboratories_code"
        varchar name "VARCHAR(150)"
        varchar building "VARCHAR(150)"
        int capacity "INT"
        varchar state "VARCHAR(30): LSTATE_ACTIVE o LSTATE_MAINTENANCE"
    }
    reservations {
        varchar reservation_id PK "VARCHAR(36), UUID"
        varchar laboratory_id FK "VARCHAR(36), fk_reservations_laboratory"
        varchar user_id FK "VARCHAR(36), fk_reservations_user"
        date reservation_date "DATE"
        time start_time "TIME"
        time end_time "TIME"
        int attendees "INT"
        varchar purpose "VARCHAR(255)"
        varchar state "VARCHAR(30): RSTATE_*"
        datetime created_at "DATETIME(6)"
    }
```

- Todas las columnas son `NOT NULL`.
- Los ids son UUID guardados como `VARCHAR(36)`, y los enums se guardan con su nombre (`VARCHAR(30)`).
- La fecha de la reserva se llama `reservation_date` porque `DATE` es palabra reservada en SQL; en Java se llama `date`.
- Las tablas usan `utf8mb4` con el cotejamiento `utf8mb4_unicode_ci`, que no distingue mayúsculas ni tildes.
- Las claves foráneas impiden borrar un usuario o un laboratorio con reservas; los servicios lo validan antes con un mensaje en español.

## Endpoints o peticiones disponibles

### Rutas web

Salen de los controladores de `adapter/ui/web`. Las rutas `/login` (POST) y `/logout` las atiende Spring Security. Toda ruta con `{id}` recibe un UUID.

| Funcionalidad | Ruta | Método | Rol | Descripción |
|---|---|---|---|---|
| Inicio | `/` | GET | Usuario, Administrador | Redirige a la pantalla principal del rol |
| Inicio de sesión | `/login` | GET | Público | Formulario de inicio de sesión |
| Inicio de sesión | `/login` | POST | Público | Valida correo y contraseña (Spring Security) |
| Cerrar sesión | `/logout` | POST | Usuario, Administrador | Cierra la sesión y vuelve al inicio de sesión |
| Mis reservas | `/reservations` | GET | Usuario | Lista las reservas del usuario |
| Nueva reserva | `/reservations/new` | GET | Usuario | Formulario con los laboratorios activos |
| Crear reserva | `/reservations` | POST | Usuario | Crea la reserva pendiente o muestra la regla incumplida |
| Cancelar reserva | `/reservations/{id}/cancel` | POST | Usuario | Cancela una reserva propia pendiente o aprobada |
| Reservas | `/admin/reservations` | GET | Administrador | Lista todas las reservas; filtro opcional `?state=` |
| Reporte PDF | `/admin/reservations/report` | GET | Administrador | Descarga el PDF entre `startDate` y `endDate` |
| Aprobar | `/admin/reservations/{id}/approve` | POST | Administrador | Aprueba una reserva pendiente |
| Rechazar | `/admin/reservations/{id}/reject` | POST | Administrador | Rechaza una reserva pendiente |
| Completar | `/admin/reservations/{id}/complete` | POST | Administrador | Completa una reserva aprobada |
| Cancelar | `/admin/reservations/{id}/cancel` | POST | Administrador | Cancela cualquier reserva pendiente o aprobada |
| Laboratorios | `/admin/laboratories` | GET | Administrador | Lista los laboratorios |
| Nuevo laboratorio | `/admin/laboratories/new` | GET | Administrador | Formulario de laboratorio |
| Crear laboratorio | `/admin/laboratories` | POST | Administrador | Crea el laboratorio o muestra la regla incumplida |
| Cambiar estado | `/admin/laboratories/{id}/toggle-state` | POST | Administrador | Alterna entre activo y mantenimiento |
| Usuarios | `/admin/users` | GET | Administrador | Lista nombre, correo y rol |
| Nuevo usuario | `/admin/users/new` | GET | Administrador | Formulario de usuario |
| Crear usuario | `/admin/users` | POST | Administrador | Crea el usuario con la contraseña cifrada |
| Eliminar usuario | `/admin/users/{id}/delete` | POST | Administrador | Elimina un usuario sin reservas que no sea el propio |

Un rol sin permiso recibe la página 403; una ruta que no existe, la 404; y sin sesión, se redirige al inicio de sesión. Los formularios POST llevan el token CSRF que agrega Thymeleaf.

### API REST

Escucha en el puerto **7070**, recibe y devuelve JSON, y usa autenticación **HTTP Basic** con los mismos usuarios de la web. Ninguna respuesta incluye contraseñas.

#### Endpoints

| Método | Ruta | Rol | Respuesta |
|---|---|---|---|
| GET | `/api/laboratories` | Usuario, Administrador | 200, lista de laboratorios |
| GET | `/api/laboratories/{laboratoryId}` | Usuario, Administrador | 200, un laboratorio |
| POST | `/api/laboratories` | Administrador | 201, el laboratorio creado |
| POST | `/api/laboratories/{laboratoryId}/toggle-state` | Administrador | 200, el laboratorio con el nuevo estado |
| GET | `/api/reservations` (opcional `?state=`) | Usuario, Administrador | 200, lista de reservas: el usuario solo ve las suyas |
| GET | `/api/reservations/{reservationId}` | Usuario, Administrador | 200, una reserva: el usuario solo puede ver las suyas |
| POST | `/api/laboratories/{laboratoryId}/reservations` | Usuario | 201, la reserva creada, pendiente |
| POST | `/api/reservations/{reservationId}/approve` | Administrador | 200, la reserva aprobada |
| POST | `/api/reservations/{reservationId}/reject` | Administrador | 200, la reserva rechazada |
| POST | `/api/reservations/{reservationId}/complete` | Administrador | 200, la reserva completada |
| POST | `/api/reservations/{reservationId}/cancel` | Usuario, Administrador | 200, la reserva cancelada |

#### Errores

Todos los errores tienen la misma forma, con el mensaje en español:

```json
{"error":"El laboratorio ya tiene una reserva en ese horario"}
```

| Código | Cuándo |
|---|---|
| 400 | Se incumple una regla de negocio, el JSON no es válido o el id no tiene formato de UUID |
| 401 | Faltan las credenciales o son incorrectas |
| 403 | El rol no permite la acción, o se consulta una reserva de otro usuario |
| 404 | No existe el laboratorio, la reserva o la ruta |
| 500 | Error interno, sin mostrar detalles |

#### Ejemplos con curl

Primero crea un usuario con rol Usuario desde la web, en **Usuarios** (<http://localhost:8080/admin/users>), y pon su correo y su contraseña en `USUARIO`. La fecha debe ser futura.

```bash
API=http://localhost:7070/api
USUARIO='correo-del-usuario:su-contraseña'
ADMIN='admin@usantoto.edu.co:Admin2026*'
FECHA=$(date -d '+20 days' +%F)
```

Crear un laboratorio (al principio no hay ninguno):

```bash
curl -u "$ADMIN" -H 'Content-Type: application/json' \
  -d '{"code":"LAB-BIO-01","name":"Laboratorio de Biología","building":"Sede Central - Bloque C","capacity":20,"state":"LSTATE_ACTIVE"}' \
  "$API/laboratories"
```

Copia el `laboratoryId` de la respuesta. En modo `inmemory` los datos se pierden al cerrar, así que cambia en cada arranque.

```bash
LAB_ID=pega-aqui-el-laboratoryId
```

Listar los laboratorios:

```bash
curl -u "$USUARIO" "$API/laboratories"
```

Ver un laboratorio:

```bash
curl -u "$USUARIO" "$API/laboratories/$LAB_ID"
```

Cambiar un laboratorio entre activo y mantenimiento. Repite el comando para dejarlo como estaba:

```bash
curl -u "$ADMIN" -X POST "$API/laboratories/$LAB_ID/toggle-state"
```

Crear una reserva:

```bash
curl -u "$USUARIO" -H 'Content-Type: application/json' \
  -d '{"date":"'"$FECHA"'","startTime":"08:00","endTime":"10:00","attendees":15,"purpose":"Práctica de programación"}' \
  "$API/laboratories/$LAB_ID/reservations"
```

Copia el `reservationId` de la respuesta:

```bash
RESERVATION_ID=pega-aqui-el-reservationId
```

Listar las reservas, y solo las pendientes:

```bash
curl -u "$USUARIO" "$API/reservations"
curl -u "$ADMIN" "$API/reservations?state=RSTATE_PENDING"
```

Ver una reserva:

```bash
curl -u "$USUARIO" "$API/reservations/$RESERVATION_ID"
```

Aprobar una reserva pendiente y, después, completarla:

```bash
curl -u "$ADMIN" -X POST "$API/reservations/$RESERVATION_ID/approve"
curl -u "$ADMIN" -X POST "$API/reservations/$RESERVATION_ID/complete"
```

Rechazar una reserva pendiente (crea otra y copia su `reservationId`):

```bash
curl -u "$ADMIN" -X POST "$API/reservations/$RESERVATION_ID/reject"
```

Cancelar una reserva pendiente o aprobada (crea otra y copia su `reservationId`):

```bash
curl -u "$USUARIO" -X POST "$API/reservations/$RESERVATION_ID/cancel"
```

### Reporte PDF

El administrador lo descarga desde la web, en **Reservas** (<http://localhost:8080/admin/reservations>), eligiendo la fecha inicial y la final. El archivo se llama `reservas_<inicio>_<fin>.pdf` y contiene:

- Título, rango de fechas, y fecha y hora de generación (hora de Colombia).
- Tabla con laboratorio, solicitante, fecha, horario, asistentes y estado. Incluye las reservas del rango en cualquier estado, con ambas fechas incluidas, ordenadas por fecha y hora.
- Resumen con el total de reservas por estado y el total general.
- Número de página en el pie.

Si no hay reservas en el rango, el reporte lo indica. Si las fechas faltan o están invertidas, la web muestra el error.

## Stack de tecnologías

Versiones resueltas con `./gradlew dependencies` (dependencias directas del `build.gradle.kts`).

| Dependencia | Versión | Uso |
|---|---|---|
| `org.springframework.boot:spring-boot-starter-webmvc` | 4.1.2 | Spring MVC y Tomcat |
| `org.springframework.boot:spring-boot-starter-thymeleaf` | 4.1.2 | Vistas Thymeleaf |
| `org.springframework.boot:spring-boot-starter-security` | 4.1.2 | Inicio de sesión, roles y BCrypt |
| `org.springframework.boot:spring-boot-starter-jdbc` | 4.1.2 | `DataSource` (HikariCP) y carga de `schema.sql` y `data.sql` |
| `io.javalin:javalin` | 7.2.2 | API REST |
| `org.jdbi:jdbi3-core` | 3.55.0 | Acceso a la base con SQL |
| `org.jdbi:jdbi3-sqlobject` | 3.55.0 | DAO declarativos (`@SqlQuery`, `@SqlUpdate`) |
| `org.mariadb.jdbc:mariadb-java-client` | 3.5.10 | Driver de MariaDB (versión gestionada por Spring Boot) |
| `com.github.librepdf:openpdf` | 2.0.5 | Reporte PDF (última versión compatible con Java 17) |
| `org.springframework.boot:spring-boot-devtools` | 4.1.2 | Reinicio automático en desarrollo |
| `org.springframework.boot:spring-boot-starter-webmvc-test` | 4.1.2 | Tests (incluye JUnit Jupiter 6.0.3) |
| `org.junit.platform:junit-platform-launcher` | 6.0.3 | Ejecución de los tests con Gradle |
| BOM `org.eclipse.jetty.ee10:jetty-ee10-bom` | 12.1.14 | Alinea el Jetty de Javalin con el de Spring Boot |

| Herramienta | Versión |
|---|---|
| Java | 17 (toolchain de Gradle) |
| Gradle (wrapper) | 9.9.1, con Kotlin DSL |
| Plugin `org.springframework.boot` | 4.1.2 |
| Plugin `io.spring.dependency-management` | 1.1.7 |
| Bootstrap (CSS y JS en `static/`) | 5.3.8 |
| MariaDB | Servidor local; probado con 13.0.2 |

Versiones que traen las dependencias anteriores: Spring Framework 7.0.10, Spring Security 7.1.2, Thymeleaf 3.1.5, Jackson 3.1.7, Tomcat 11.0.27, Jetty 12.1.14 y HikariCP 7.0.2.

## Capturas UI/UX

Tomadas con la aplicación en modo `mariadb` y los [datos de demostración](#datos-de-demostración), en una ventana de 1440 px de ancho.

### Inicio de sesión

Pantalla de entrada: correo y contraseña, con Spring Security.

![Inicio de sesión](docs/capturas/01-login.png)

### Mis reservas

Un usuario ve solo sus reservas, con su estado, y puede cancelar las pendientes o aprobadas.

![Mis reservas](docs/capturas/02-mis-reservas.png)

### Nueva reserva

Formulario con los laboratorios activos, la franja permitida (06:00 a 22:00, máximo 4 horas) y el número de asistentes.

![Nueva reserva](docs/capturas/03-nueva-reserva.png)

### Alerta de cruce de horario

Si la franja se cruza con otra reserva pendiente o aprobada del mismo laboratorio, se muestra la regla incumplida y se conservan los datos.

![Alerta de cruce de horario](docs/capturas/04-alerta-cruce.png)

### Reservas del administrador

Todas las reservas con el filtro por estado (aquí, pendientes), las acciones para aprobar, rechazar o cancelar y el formulario del reporte PDF.

![Reservas del administrador](docs/capturas/05-reservas-administrador.png)

### Laboratorios

El administrador ve los laboratorios con su capacidad y estado, y los alterna entre activo y mantenimiento.

![Laboratorios](docs/capturas/06-laboratorios.png)

### Usuarios

Lista de usuarios con nombre, correo y rol, sin contraseñas; el administrador no puede eliminarse a sí mismo.

![Usuarios](docs/capturas/07-usuarios.png)

### Nuevo usuario

Formulario con confirmación de contraseña; la contraseña se guarda como hash BCrypt.

![Nuevo usuario](docs/capturas/08-nuevo-usuario.png)

### Reporte PDF

Reporte generado con OpenPDF, abierto en el navegador: tabla de reservas del rango y resumen por estado.

![Reporte PDF](docs/capturas/09-reporte-pdf.png)

### Página 403

Un usuario sin el rol necesario recibe una página de error propia, en español.

![Página 403](docs/capturas/10-pagina-403.png)

### API REST

Respuesta JSON de `GET /api/laboratories` en el navegador, con autenticación HTTP Basic.

![API REST](docs/capturas/11-api-rest.png)

## Análisis personal

<!-- BORRADOR: análisis personal escrito a partir de los retos del proyecto. Revisarlo, ajustarlo con la experiencia de cada integrante y quitar este comentario antes de entregar. -->

### Retos

- **JPA o JDBI.** Los lineamientos se contradecían: en una parte pedían JPA y en otra JDBI. Elegimos JDBI y escribimos el SQL a mano. Eso nos obligó a crear las tablas con `schema.sql`, a traducir cada fila al dominio con los records `XJdbi` y a entender qué hace por debajo un ORM.
- **Javalin 7 y Jackson 3.** Casi todos los ejemplos que encontramos eran de Javalin 6, y la API cambió: las rutas, los filtros y los errores ahora se configuran dentro de `Javalin.create(config -> …)`. Además, Spring Boot 4 trae Jackson 3 (paquete `tools.jackson`), así que conectamos Javalin con el `JsonMapper` de Spring mediante `JavalinJackson3` en lugar de agregar Jackson 2.
- **Versiones de Jetty.** Javalin usa los módulos ee10 de Jetty, que Spring Boot 4 no gestiona. Para no mezclar versiones importamos el BOM `jetty-ee10-bom` en la misma versión de Jetty de Spring Boot (12.1.14).
- **OpenPDF y Java 17.** La última versión de OpenPDF no corría en Java 17. Revisando el bytecode de los jars descubrimos que desde la 2.1.0 exige Java 21, así que bajamos a la 2.0.5, la última compatible.
- **De H2 a MariaDB.** Empezamos con H2 en memoria mientras nos asignaban la base. Al migrar a MariaDB tuvimos que quitar los `DROP TABLE`, hacer que `data.sql` no duplicara datos (`INSERT IGNORE`), leer la contraseña de una variable de entorno y elegir un cotejamiento que guardara bien las tildes y la ñ.
- **Reorganizar los paquetes.** Con las correcciones del profesor movimos cada configuración al paquete de su repositorio, agrupamos la web y la API en `adapter/ui` y renombramos las clases de persistencia como `MariaDb…`. Como el dominio no dependía de los adaptadores, el cambio no tocó ninguna regla de negocio.
- **La regla de cruces.** Definimos el cruce como `nuevaInicio < existenteFin && nuevaFin > existenteInicio`, de modo que 10:00–12:00 y 12:00–14:00 no se cruzan y solo cuentan las reservas pendientes o aprobadas. La probamos con sus casos límite (06:00, 22:00, 4 horas exactas, capacidad exacta) y con un reloj fijo (`Clock.fixed`), para que los tests no dependieran de la fecha real.

### Aprendizajes

- **Arquitectura hexagonal.** Pudimos cambiar los repositorios en memoria por MariaDB sin tocar las entidades ni los servicios: solo agregamos otros adaptadores que implementan los mismos puertos.
- **Inyección de dependencias.** Spring crea los repositorios y los servicios en las clases de configuración y se los entrega a los controladores; con una propiedad (`labslot.repository`) elegimos qué implementación usar.
- **Spring Security con roles.** Protegimos las rutas por rol, cifrando las contraseñas con BCrypt detrás de un puerto propio, para que el dominio no dependiera del framework.
- **JDBI con SQL explícito.** Ver cada consulta nos ayudó a entender los tipos de MariaDB, las claves foráneas y los cotejamientos.
- **Pruebas sin base de datos.** Los tests arman los servicios con los repositorios en memoria y un reloj fijo, y corren en segundos.
- **Los casos límite importan.** Muchos errores aparecían justo en los bordes: la hora de cierre, la capacidad exacta, la longitud máxima de un texto o una contraseña con ñ.
