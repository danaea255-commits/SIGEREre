-- Solo ejecutar si ya tenías la base de la versión anterior (PHP)
USE saboros;

-- Agregar columnas si no existen (MySQL 8+ / MariaDB)
-- Si da error de "columna ya existe", ignóralo.

ALTER TABLE pedidos 
    ADD COLUMN IF NOT EXISTS detalle TEXT,
    ADD COLUMN IF NOT EXISTS pagado TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS entregado TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS fecha DATETIME DEFAULT CURRENT_TIMESTAMP;

-- Crear tabla de detalle si no existe
CREATE TABLE IF NOT EXISTS detalle_pedido (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    pedido_id       INT NOT NULL,
    platillo_id     INT NOT NULL,
    cantidad        INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedidos(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_detalle_platillo
        FOREIGN KEY (platillo_id) REFERENCES platillos(id)
        ON DELETE RESTRICT
) ENGINE=InnoDB;
