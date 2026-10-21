SET NAMES utf8mb4;

DROP TABLE IF EXISTS reserva_recurso;
DROP TABLE IF EXISTS reserva_categoria;
DROP TABLE IF EXISTS reserva;
DROP TABLE IF EXISTS recurso;


CREATE TABLE recurso (
     id           VARCHAR(20)  NOT NULL,
     nombre       VARCHAR(100) NOT NULL,
     descripcion  VARCHAR(255) NOT NULL,
     id_categoria INT          NOT NULL,
     PRIMARY KEY (id),
     KEY idx_recurso_categoria (id_categoria),
     CONSTRAINT fk_recurso_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reserva (
    id             INT          NOT NULL AUTO_INCREMENT,
    id_funcionario INT          NOT NULL,
    actividad      VARCHAR(150) NOT NULL,
    fecha          DATE         NOT NULL,
    hora_inicio    TIME         NOT NULL,
    hora_fin       TIME         NOT NULL,
    estado         ENUM('ACTIVA','CANCELADA') NOT NULL DEFAULT 'ACTIVA',
    PRIMARY KEY (id),
    KEY idx_reserva_fecha (fecha),
    KEY idx_reserva_funcionario (id_funcionario),
    CONSTRAINT fk_reserva_funcionario FOREIGN KEY (id_funcionario) REFERENCES usuario (id),
    CONSTRAINT chk_reserva_horas CHECK (hora_inicio < hora_fin)   -- se aplica en MySQL 8.0.16 o superior
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reserva_categoria (
    id_reserva   INT NOT NULL,
    id_categoria INT NOT NULL,
    PRIMARY KEY (id_reserva, id_categoria),
    KEY idx_rc_categoria (id_categoria),
    CONSTRAINT fk_rc_reserva   FOREIGN KEY (id_reserva)   REFERENCES reserva (id),
    CONSTRAINT fk_rc_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reserva_recurso (
    id_reserva INT         NOT NULL,
    id_recurso VARCHAR(20) NOT NULL,
    PRIMARY KEY (id_reserva, id_recurso),
    KEY idx_rr_recurso (id_recurso),
    CONSTRAINT fk_rr_reserva FOREIGN KEY (id_reserva) REFERENCES reserva (id),
    CONSTRAINT fk_rr_recurso FOREIGN KEY (id_recurso) REFERENCES recurso (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------- SEMILLA (de data/recursos.xml y data/reservas.xml) ----------------
-- Requiere que el script de I1 ya haya insertado, con id explícito:
--   categorías 1, 2 y 3  y  el usuario con id 1234.
INSERT INTO recurso (id, nombre, descripcion, id_categoria) VALUES
    ('1111', 'Laptop','Laptop Dell 19"',1),
    ('1112', 'Proyector','Proyector con con cables Respectivos',  1),
    ('2221', 'Salon Grande',  'Salon Grande para Eventos',2),
    ('2222', 'Salon Mediano', 'Salon Mediano Para Eventos',2),
    ('2223', 'Salon Pequeño', 'Salon Pequeño para Eventos', 2),
    ('3331', 'Sillas', 'Sillas Plegables',3),
    ('3332', 'Mesas','Mesa Plegables', 3);

INSERT INTO reserva (id, id_funcionario, actividad, fecha, hora_inicio, hora_fin, estado)
VALUES (1111, 1234, 'reservar laptop', '2026-09-10', '10:00:00', '11:00:00', 'ACTIVA');
INSERT INTO reserva_categoria (id_reserva, id_categoria) VALUES (1111, 1);
INSERT INTO reserva_recurso   (id_reserva, id_recurso)   VALUES (1111, '1111');

-- La reserva semilla usó el id 1111 a mano: el siguiente automático debe ser 1112.
ALTER TABLE reserva AUTO_INCREMENT = 1112;
