-- SOLO para MySQL instalado localmente (Workbench). Con Docker NO es necesario:
-- compose.yaml crea las bases solo.
-- Ejecutar con una cuenta administradora. Cambia las contraseñas y cópialas en .env.
-- Las TABLAS no se crean aquí: Hibernate las crea a partir de las clases @Entity (ddl-auto: update).

CREATE DATABASE IF NOT EXISTS animate_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS animate_catalogo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS animate_pedidos  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Una cuenta por microservicio, cada una con acceso SOLO a su base.
CREATE USER IF NOT EXISTS 'animate_usuarios'@'localhost' IDENTIFIED BY 'CambiarUsuarios123!';
CREATE USER IF NOT EXISTS 'animate_catalogo'@'localhost' IDENTIFIED BY 'CambiarCatalogo123!';
CREATE USER IF NOT EXISTS 'animate_pedidos'@'localhost'  IDENTIFIED BY 'CambiarPedidos123!';

GRANT ALL PRIVILEGES ON animate_usuarios.* TO 'animate_usuarios'@'localhost';
GRANT ALL PRIVILEGES ON animate_catalogo.* TO 'animate_catalogo'@'localhost';
GRANT ALL PRIVILEGES ON animate_pedidos.*  TO 'animate_pedidos'@'localhost';
FLUSH PRIVILEGES;
