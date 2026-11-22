-- Datos de prueba de LabSlot (USTA Tunja), los mismos de InMemoryConfig.sampleData.
-- Las fechas son relativas al día de la primera carga. INSERT IGNORE deja intactas las filas que ya existen,
-- así que los arranques siguientes no duplican datos ni mueven las fechas.
-- Los UUID siguen un patrón para reconocerlos: a... usuarios, b... laboratorios, c... reservas.

-- Usuarios. Contraseñas (BCrypt): administradora = Admin2026*, los demás = Usta2026*
INSERT IGNORE INTO users (user_id, full_name, email, password, role) VALUES
('a0000000-0000-4000-8000-000000000001', 'Martha Lucía Vargas Rojas',  'martha.vargas@usantoto.edu.co',    '$2b$10$crjW.y3h5MHRi.ABMOQuJOBkWCvB0QhgC8KdEYD4fWWfsLp5FoI9a', 'ROLE_ADMIN'),
('a0000000-0000-4000-8000-000000000002', 'Jorge Andrés Castro Pinzón', 'jorge.castro@usantoto.edu.co',     '$2b$10$60oukj9.UbRFbDXtPJliFORu3jxXHOQQ3wJzhUPZAx0Uydqc8lCi2', 'ROLE_USER'),
('a0000000-0000-4000-8000-000000000003', 'Valentina Suárez Niño',      'valentina.suarez@usantoto.edu.co', '$2b$10$60oukj9.UbRFbDXtPJliFORu3jxXHOQQ3wJzhUPZAx0Uydqc8lCi2', 'ROLE_USER'),
('a0000000-0000-4000-8000-000000000004', 'Daniel Felipe Rincón Ávila', 'daniel.rincon@usantoto.edu.co',    '$2b$10$60oukj9.UbRFbDXtPJliFORu3jxXHOQQ3wJzhUPZAx0Uydqc8lCi2', 'ROLE_USER');

-- Laboratorios (LAB-QUI-01 está en mantenimiento)
INSERT IGNORE INTO laboratories (laboratory_id, code, name, building, capacity, state) VALUES
('b0000000-0000-4000-8000-000000000001', 'LAB-SIS-01', 'Laboratorio de Sistemas 1',        'Sede Central - Bloque A', 30, 'LSTATE_ACTIVE'),
('b0000000-0000-4000-8000-000000000002', 'LAB-SIS-02', 'Laboratorio de Redes y Telemática', 'Sede Central - Bloque A', 25, 'LSTATE_ACTIVE'),
('b0000000-0000-4000-8000-000000000003', 'LAB-ELE-01', 'Laboratorio de Electrónica',       'Sede Central - Bloque B', 20, 'LSTATE_ACTIVE'),
('b0000000-0000-4000-8000-000000000004', 'LAB-FIS-01', 'Laboratorio de Física',            'Sede Central - Bloque B', 24, 'LSTATE_ACTIVE'),
('b0000000-0000-4000-8000-000000000005', 'LAB-QUI-01', 'Laboratorio de Química',           'Sede Central - Bloque C', 18, 'LSTATE_MAINTENANCE');

-- Reservas. Sin cruces entre PENDING y APPROVED del mismo laboratorio y día.
INSERT IGNORE INTO reservations (reservation_id, laboratory_id, user_id, reservation_date, start_time, end_time, attendees, purpose, state, created_at) VALUES
-- Pasadas
('c0000000-0000-4000-8000-000000000001', 'b0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000002', CURRENT_DATE - INTERVAL 7 DAY, '08:00:00', '10:00:00', 28, 'Clase de Programación Orientada a Objetos',  'RSTATE_COMPLETED', TIMESTAMP(CURRENT_DATE - INTERVAL 14 DAY, '09:15:00')),
('c0000000-0000-4000-8000-000000000002', 'b0000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000003', CURRENT_DATE - INTERVAL 5 DAY, '14:00:00', '16:00:00', 15, 'Práctica de circuitos digitales',            'RSTATE_COMPLETED', TIMESTAMP(CURRENT_DATE - INTERVAL 12 DAY, '16:40:00')),
('c0000000-0000-4000-8000-000000000003', 'b0000000-0000-4000-8000-000000000005', 'a0000000-0000-4000-8000-000000000002', CURRENT_DATE - INTERVAL 4 DAY, '10:00:00', '12:00:00', 16, 'Práctica de química general',                'RSTATE_COMPLETED', TIMESTAMP(CURRENT_DATE - INTERVAL 11 DAY, '08:20:00')),
('c0000000-0000-4000-8000-000000000004', 'b0000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000004', CURRENT_DATE - INTERVAL 3 DAY, '09:00:00', '11:00:00', 10, 'Repaso grupal para el parcial de física',    'RSTATE_REJECTED',  TIMESTAMP(CURRENT_DATE - INTERVAL 8 DAY, '19:05:00')),
-- Futuras. Par contiguo en LAB-SIS-01 mañana: 10:00-12:00 y 12:00-14:00
('c0000000-0000-4000-8000-000000000005', 'b0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000002', CURRENT_DATE + INTERVAL 1 DAY, '10:00:00', '12:00:00', 30, 'Clase de Desarrollo Orientado a Servicios',  'RSTATE_APPROVED',  TIMESTAMP(CURRENT_DATE - INTERVAL 6 DAY, '10:00:00')),
('c0000000-0000-4000-8000-000000000006', 'b0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000004', CURRENT_DATE + INTERVAL 1 DAY, '12:00:00', '14:00:00', 20, 'Taller de Spring Boot del semillero',        'RSTATE_PENDING',   TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '15:30:00')),
-- CANCELLED (09:00-10:00) que se cruza con una APPROVED (08:00-11:00) en LAB-SIS-02
('c0000000-0000-4000-8000-000000000007', 'b0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000002', CURRENT_DATE + INTERVAL 2 DAY, '08:00:00', '11:00:00', 25, 'Práctica de redes: configuración de VLAN',   'RSTATE_APPROVED',  TIMESTAMP(CURRENT_DATE - INTERVAL 5 DAY, '11:45:00')),
('c0000000-0000-4000-8000-000000000008', 'b0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000003', CURRENT_DATE + INTERVAL 2 DAY, '09:00:00', '10:00:00',  6, 'Asesoría del proyecto integrador',           'RSTATE_CANCELLED', TIMESTAMP(CURRENT_DATE - INTERVAL 4 DAY, '14:10:00')),
('c0000000-0000-4000-8000-000000000009', 'b0000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000003', CURRENT_DATE + INTERVAL 3 DAY, '14:00:00', '17:00:00', 12, 'Laboratorio de microcontroladores',          'RSTATE_PENDING',   TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '09:00:00')),
-- Capacidad exacta: 24 asistentes en LAB-FIS-01 (capacidad 24)
('c0000000-0000-4000-8000-000000000010', 'b0000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000004', CURRENT_DATE + INTERVAL 4 DAY, '07:00:00', '09:00:00', 24, 'Práctica de mecánica: caída libre',          'RSTATE_APPROVED',  TIMESTAMP(CURRENT_DATE - INTERVAL 3 DAY, '17:20:00')),
-- PENDING en el mismo horario que una REJECTED en LAB-SIS-02
('c0000000-0000-4000-8000-000000000011', 'b0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000004', CURRENT_DATE + INTERVAL 7 DAY, '16:00:00', '18:00:00', 15, 'Estudio grupal de bases de datos',           'RSTATE_REJECTED',  TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '20:00:00')),
('c0000000-0000-4000-8000-000000000012', 'b0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000003', CURRENT_DATE + INTERVAL 7 DAY, '16:00:00', '18:00:00', 10, 'Ensayo de sustentación del proyecto de grado', 'RSTATE_PENDING', TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '18:30:00')),
-- 4 horas exactas y capacidad exacta (20) en LAB-ELE-01
('c0000000-0000-4000-8000-000000000013', 'b0000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000002', CURRENT_DATE + INTERVAL 8 DAY, '10:00:00', '14:00:00', 20, 'Parcial práctico de electrónica análoga',    'RSTATE_APPROVED',  TIMESTAMP(CURRENT_DATE - INTERVAL 4 DAY, '08:00:00')),
('c0000000-0000-4000-8000-000000000014', 'b0000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000003', CURRENT_DATE + INTERVAL 10 DAY, '18:00:00', '20:00:00',  8, 'Monitoría de física mecánica',               'RSTATE_PENDING',   TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '07:45:00'));
