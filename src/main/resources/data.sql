-- Datos iniciales de LabSlot: solo el administrador general. Los demás usuarios se crean desde la web.
-- Se ejecuta en cada arranque: INSERT IGNORE no duplica el usuario ni cambia el que ya existe.

-- Contraseña (BCrypt): Admin2026*. Es conocida: en un uso real hay que cambiarla (ver el README)
INSERT IGNORE INTO users (user_id, full_name, email, password, role) VALUES
('d0000000-0000-4000-8000-000000000001', 'Administrador General', 'admin@usantoto.edu.co', '$2b$10$crjW.y3h5MHRi.ABMOQuJOBkWCvB0QhgC8KdEYD4fWWfsLp5FoI9a', 'ROLE_ADMIN');
