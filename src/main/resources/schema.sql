-- Esquema de LabSlot para MariaDB. Se ejecuta en cada arranque y no borra datos: solo crea las tablas que falten.
-- utf8mb4_unicode_ci no distingue mayúsculas ni tildes al comparar (email y código únicos).
CREATE TABLE IF NOT EXISTS users (
    user_id   VARCHAR(36)  NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email     VARCHAR(150) NOT NULL,
    password  VARCHAR(100) NOT NULL,
    role      VARCHAR(30)  NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_users_email UNIQUE (email)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS laboratories (
    laboratory_id VARCHAR(36)  NOT NULL,
    code          VARCHAR(20)  NOT NULL,
    name          VARCHAR(150) NOT NULL,
    building      VARCHAR(150) NOT NULL,
    capacity      INT          NOT NULL,
    state         VARCHAR(30)  NOT NULL,
    PRIMARY KEY (laboratory_id),
    CONSTRAINT uk_laboratories_code UNIQUE (code)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- La fecha se llama reservation_date porque DATE es palabra clave en SQL
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id   VARCHAR(36)  NOT NULL,
    laboratory_id    VARCHAR(36)  NOT NULL,
    user_id          VARCHAR(36)  NOT NULL,
    reservation_date DATE         NOT NULL,
    start_time       TIME         NOT NULL,
    end_time         TIME         NOT NULL,
    attendees        INT          NOT NULL,
    purpose          VARCHAR(255) NOT NULL,
    state            VARCHAR(30)  NOT NULL,
    created_at       DATETIME(6)  NOT NULL,
    PRIMARY KEY (reservation_id),
    CONSTRAINT fk_reservations_laboratory FOREIGN KEY (laboratory_id) REFERENCES laboratories (laboratory_id),
    CONSTRAINT fk_reservations_user       FOREIGN KEY (user_id)       REFERENCES users (user_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
