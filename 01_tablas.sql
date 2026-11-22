-- ============================================================
-- SIGERE - Esquema de base de datos (versión mejorada)
-- Ejecutar en MySQL / MariaDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS saboros CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE saboros;

-- ----------------------------------------------------------
-- Tabla de usuarios
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    correo      VARCHAR(100) NOT NULL UNIQUE,
    clave       VARCHAR(255) NOT NULL,
    rol         ENUM('Administrador', 'Mesero') NOT NULL DEFAULT 'Mesero',
    creado_en   DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Tabla de platillos / menú
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS platillos (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    precio      DECIMAL(10,2) NOT NULL,
    activo      TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Tabla de mesas
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS mesas (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL,
    zona        VARCHAR(30) NOT NULL,
    estado      ENUM('libre', 'ocupada', 'reservada', 'limpieza') NOT NULL DEFAULT 'libre'
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Tabla de pedidos (cabecera)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS pedidos (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    mesa_id         INT NOT NULL,
    cliente         VARCHAR(100),
    notas           TEXT,
    estado_cocina   ENUM('Pendiente', 'En Preparación', 'Listo') NOT NULL DEFAULT 'Pendiente',
    total           DECIMAL(10,2) NOT NULL DEFAULT 0,
    detalle         TEXT,
    pagado          TINYINT(1) NOT NULL DEFAULT 0,
    entregado       TINYINT(1) NOT NULL DEFAULT 0,
    fecha           DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_mesa
        FOREIGN KEY (mesa_id) REFERENCES mesas(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ----------------------------------------------------------
-- Tabla de detalle de pedidos (normalizada)
-- ----------------------------------------------------------
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

-- ----------------------------------------------------------
-- Índices para consultas frecuentes
-- ----------------------------------------------------------
CREATE INDEX idx_pedidos_pagado      ON pedidos(pagado);
CREATE INDEX idx_pedidos_entregado   ON pedidos(entregado);
CREATE INDEX idx_pedidos_estado      ON pedidos(estado_cocina);
CREATE INDEX idx_pedidos_fecha       ON pedidos(fecha);
CREATE INDEX idx_mesas_estado        ON mesas(estado);

-- ----------------------------------------------------------
-- Usuario de aplicación (desarrollo)
-- ----------------------------------------------------------
CREATE USER IF NOT EXISTS 'sigere_user'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON saboros.* TO 'sigere_user'@'localhost';
FLUSH PRIVILEGES;
