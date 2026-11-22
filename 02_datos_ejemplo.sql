USE saboros;

-- Usuarios de ejemplo
INSERT IGNORE INTO usuario (nombre, correo, clave, rol) VALUES
('Administrador', 'admin@sigere.com', 'admin123', 'Administrador'),
('Carlos Mendoza', 'carlos@sigere.com', 'mesero123', 'Mesero');

-- Mesas
INSERT INTO mesas (nombre, zona) VALUES
('Mesa VIP 1', 'VIP'),
('Mesa VIP 2', 'VIP'),
('Mesa G-1', 'General'),
('Mesa G-2', 'General'),
('Mesa G-3', 'General'),
('Mesa G-4', 'General'),
('Terraza 1', 'Terraza'),
('Terraza 2', 'Terraza');

-- Platillos
INSERT INTO platillos (nombre, precio) VALUES
('Tacos al Pastor', 85.00),
('Sopa de Res', 75.00),
('Bistec a la Mexicana', 130.00),
('Flan Napolitano', 45.00),
('Agua de Horchata 1L', 40.00),
('Enchiladas Suizas', 110.00);
