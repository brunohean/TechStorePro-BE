-- 1. Insertar Roles iniciales
INSERT INTO roles (nombre) VALUES ('ROLE_USER');
INSERT INTO roles (nombre) VALUES ('ROLE_ADMIN');

-- 2. Insertar Productos para el catálogo de TechStore Pro
INSERT INTO productos (nombre, descripcion, precio, stock, imagen_url)
VALUES ('Teclado Mecánico RGB', 'Teclado switch blue con retroiluminación', 45.90, 15, 'https://link-imagen.com/teclado.jpg');

INSERT INTO productos (nombre, descripcion, precio, stock, imagen_url)
VALUES ('Mouse Gamer Pro', 'Sensor óptico de 16000 DPI', 29.50, 20, 'https://link-imagen.com/mouse.jpg');

INSERT INTO productos (nombre, descripcion, precio, stock, imagen_url)
VALUES ('Audífonos Noise Cancelling', 'Bluetooth 5.0 con cancelación activa', 89.00, 10, 'https://link-imagen.com/audifonos.jpg');

INSERT INTO productos (nombre, descripcion, precio, stock, imagen_url)
VALUES ('Monitor 27" 144Hz', 'Panel IPS resolución 2K', 299.00, 5, 'https://link-imagen.com/monitor.jpg');