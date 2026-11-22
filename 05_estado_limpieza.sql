USE saboros;

ALTER TABLE mesas
    MODIFY COLUMN estado ENUM('libre', 'ocupada', 'reservada', 'limpieza') NOT NULL DEFAULT 'libre';
