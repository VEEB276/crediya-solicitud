-- Create target database if not exists (uses Flyway placeholder ${db})
CREATE DATABASE IF NOT EXISTS `${db}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create estados table
CREATE TABLE IF NOT EXISTS `${db}`.`estados` (
                                                 `id_estado` BIGINT NOT NULL AUTO_INCREMENT,
                                                 `nombre` VARCHAR(100) NOT NULL,
    `sigla` VARCHAR(10) NOT NULL UNIQUE,
    `descripcion` VARCHAR(255) NULL,
    PRIMARY KEY (`id_estado`)
    ) ENGINE=InnoDB;

-- Create tipo prestamo table
CREATE TABLE IF NOT EXISTS `${db}`.`tipo_prestamo` (
                                                       `id_tipo_prestamo` BIGINT NOT NULL AUTO_INCREMENT,
                                                       `nombre` VARCHAR(100) NOT NULL,
    `sigla` VARCHAR(10) NOT NULL UNIQUE,
    `descripcion` VARCHAR(255) NULL,
    `monto_minimo` DECIMAL(15,2) NULL,
    `monto_maximo` DECIMAL(15,2) NULL,
    `tasa_interes` DECIMAL(5,2) NULL,
    `validacion_automatica` TINYINT(1) NULL,
    PRIMARY KEY (`id_tipo_prestamo`)
    ) ENGINE=InnoDB;

-- Create solicitud table
CREATE TABLE IF NOT EXISTS `${db}`.`solicitud` (
                                                   `id_solicitud` BIGINT NOT NULL AUTO_INCREMENT,
                                                   `documento_identidad` VARCHAR(50) NOT NULL,
    `monto` DECIMAL(15,2) NULL,
    `plazo` INT NULL,
    `email` VARCHAR(150) NULL,
    `id_estado` BIGINT NULL,
    `id_tipo_prestamo` BIGINT NULL,
    PRIMARY KEY (`id_solicitud`),
    CONSTRAINT `fk_solicitud_estados` FOREIGN KEY (`id_estado`) REFERENCES `${db}`.`estados`(`id_estado`)
    ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT `fk_solicitud_prestamo` FOREIGN KEY (`id_tipo_prestamo`) REFERENCES `${db}`.`tipo_prestamo`(`id_tipo_prestamo`)
    ON UPDATE CASCADE ON DELETE SET NULL
    ) ENGINE=InnoDB;

-- Helpful index for FK
CREATE INDEX `idx_solicitud_id_estado` ON `${db}`.`solicitud` (`id_estado`);
CREATE INDEX `idx_solicitud_id_tipo_prestamo` ON `${db}`.`solicitud` (`id_tipo_prestamo`);

-- Insert sample data into tipo_prestamo
INSERT INTO `${db}`.`tipo_prestamo`
(`nombre`, `sigla`, `descripcion`, `monto_minimo`, `monto_maximo`, `tasa_interes`, `validacion_automatica`)
VALUES
    ('Préstamo Personal', 'PER','Préstamo para gastos personales', 500000, 10000000, 12.5, 1),
    ('Préstamo Vehicular', 'VEH', 'Préstamo para compra de vehículo', 5000000, 50000000, 10.0, 0),
    ('Préstamo Hipotecario', 'HIP', 'Préstamo para adquisición de vivienda', 20000000, 200000000, 8.5, 0),
    ('Préstamo Educativo', 'EDU', 'Préstamo para estudios universitarios', 1000000, 30000000, 9.0, 1);

-- Insert sample data into estados
INSERT INTO `${db}`.`estados`
(`nombre`, `sigla`, `descripcion`)
VALUES
    ('Pendiente de revisión', 'PEN', 'Solicitud creada pero aún no procesada'),
    ('Aprobado', 'APR', 'Solicitud aprobada y en trámite de desembolso'),
    ('Rechazado', 'REJ', 'Solicitud revisada y rechazada'),
    ('Cancelado', 'CAN', 'Solicitud cancelada por el usuario o el sistema');
