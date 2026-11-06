# backend-animate — Backend de la tienda Animate (DSY1104 · EP3)

Backend en **Spring Boot 3.5** con **3 microservicios + API Gateway**, una base de datos por servicio
(**H2** para desarrollo y **MySQL en Docker**), **JWT con roles y renovación de sesión** y **Swagger**.
Basado en la arquitectura del proyecto de referencia GameTech EV3 y adaptado al frontend `Animate-React`.

| Aplicación | Puerto | Responsabilidad | Base propia |
|---|---:|---|---|
| `usuarios-service` | 8081 | Registro, login, JWT, refresh, perfil, CRUD de usuarios, direcciones y tarjetas | `animate_usuarios` |
| `catalogo-service` | 8082 | CRUD de mangas (con tomos) y figuras (con galería), papelera, precios y stock | `animate_catalogo` |
| `pedidos-service` | 8083 | Compras, historial del usuario, ventas para el admin y estados de entrega | `animate_pedidos` |
| `api-gateway` | 8080 | Única entrada del frontend: rutas, CORS y Swagger agregado | sin base |
| `common` | — | Librería compartida (errores, seguridad JWT, OpenAPI). **No es un microservicio** | — |

```
React (5173) ──HTTP──► API Gateway (8080) ──► usuarios (8081) ──► animate_usuarios
                                          ├──► catalogo (8082) ──► animate_catalogo
                                          └──► pedidos  (8083) ──► animate_pedidos
                                                   └── HTTP interno ──► catalogo /internal/ventas
```

## Capas de cada microservicio

| Paquete | Qué hace | Ejemplo |
|---|---|---|
| `controller` | Recibe HTTP y delega en el servicio | `MangasController` |
| `dto` | Datos que entran/salen de la API, con validaciones | `MangaRequest`, `MangaResponse` |
| `service` | Reglas de negocio y transacciones (`@Transactional`) | `MangasService` |
| `repository` | Acceso a datos con Spring Data JPA | `MangasRepository` |
| `model` | Entidades `@Entity` = tablas | `Manga`, `Tomo` |
| `exception` | Convierte errores en JSON + estado HTTP (`@ControllerAdvice`) | `GlobalExceptionHandler` |
| `config` | Seguridad por rol y datos iniciales | `SecurityConfig`, `Seed` |
| `security` (Usuarios) | Claves RSA para firmar JWT | `JwtKeys` |
| `mapper` (Pedidos) | Entidad → DTO | `PedidoMapper` |

Las tablas **no se escriben a mano**: Hibernate las crea a partir de las clases `@Entity`
(`ddl-auto: update`), y Spring Data genera las consultas SQL a partir del nombre de los métodos
(`findByActivoTrueOrderByIdAsc`). Solo hay 4 consultas JPQL escritas a mano (`@Query` + `@Lock`) para
bloquear filas al descontar stock o al renovar sesión.

## Requisitos

- Java 17 o superior (recomendado JDK 21).
- Internet la primera vez (Maven descarga las dependencias).
- Python 3.10+ solo para los scripts de ayuda (opcional).
- Docker Desktop solo para la opción con MySQL.

## Opción 1: ejecutar local con H2 (sin instalar base de datos)

```bash
cd backend-animate
python scripts/generar_env.py        # crea .env con claves aleatorias

# Windows
.\mvnw.cmd verify                    # compila y ejecuta las pruebas
# Linux / macOS
chmod +x mvnw && ./mvnw verify

python scripts/iniciar_local.py      # levanta los 4 servicios
```

Cada servicio guarda su base H2 en `.run/<servicio>/data`. La clave privada RSA se genera sola en
`.run/usuarios/data/keys` (no se sube a Git). También puedes iniciar cada JAR en su propia terminal con
`java -jar <modulo>/target/<modulo>-1.0.0.jar` (primero usuarios).

## Opción 2: Docker con MySQL (recomendada para la entrega / EC2)

```bash
cd backend-animate
python scripts/generar_env.py
docker compose up --build
```

Levanta 3 MySQL 8.4 (cada uno con su volumen), los 3 microservicios con perfil `mysql` y el Gateway.
**Solo el Gateway publica el puerto 8080.** Cada microservicio tiene su propio `Dockerfile`.

```bash
docker compose down        # detiene y CONSERVA los datos
docker compose down -v     # detiene y BORRA las bases
```

## Opción 3: MySQL instalado localmente (Workbench)

1. Ejecuta `sql/01_crear_bases_mysql.sql` como administrador (cambia las contraseñas).
2. Pon esas contraseñas en `.env` (`DB_USUARIOS_PASSWORD`, etc.).
3. `python scripts/iniciar_local.py --perfil mysql`

## Cuentas iniciales

| Correo | Contraseña | Rol |
|---|---|---|
| ana@email.com | AdminAnimate#1 | ADMIN |
| carlos@email.com | Carlos#Laraña1 | USUARIO |

El catálogo inicial (20 mangas y 3 figuras) se carga desde `catalogo-service/src/main/resources/seed/`,
copiado de `src/data/mangas.js` y `figuras.js` del frontend. Solo se carga si las tablas están vacías.

## Swagger

Abre **http://localhost:8080/swagger-ui.html** y elige el servicio en el selector (arriba a la derecha).

1. En *Usuarios*, ejecuta `POST /api/auth/login` con `{"correo":"ana@email.com","contrasenia":"AdminAnimate#1"}`.
   La cabecera `X-Requested-With` debe valer `Animate` (ya viene puesta).
2. Copia `accessToken`, pulsa **Authorize** y pégalo.
3. Cambia a *Catálogo* o *Pedidos* y vuelve a autorizar si hace falta.

## Seguridad (JWT con roles)

- **Contraseñas** con BCrypt. El registro público siempre crea rol `USUARIO`.
- **Access token**: JWT firmado con RS256, dura 15 min. Lleva `sub` (id), `roles`, `correo` y `nombre`.
  Catálogo y Pedidos lo validan con la clave pública que Usuarios publica en `/api/auth/jwks`.
- **Refresh token**: valor aleatorio en una cookie `HttpOnly`, `SameSite=Strict`, ruta `/api/auth`,
  7 días. En la base solo se guarda su hash SHA-256. Cada renovación lo **rota** (el anterior deja de servir).
- Las rutas que usan la cookie exigen la cabecera `X-Requested-With: Animate` y un origen permitido.
- Cada servicio aplica permisos por ruta y rol en su `SecurityConfig` (401 sin sesión, 403 sin permiso).

## Endpoints (todos vía `http://localhost:8080`)

### Usuarios (8081)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | público | Login con correo y contraseña |
| POST | `/api/auth/registro` | público | Registro (rol USUARIO) |
| POST | `/api/auth/refresh` | cookie | Renueva el JWT (al recargar la página) |
| POST | `/api/auth/logout` | cookie | Cierra sesión |
| GET | `/api/auth/me` | sesión | Datos del usuario del token |
| PUT | `/api/usuarios/me` | sesión | Editar mi perfil (y contraseña opcional) |
| GET | `/api/usuarios` | ADMIN | Listar usuarios |
| GET | `/api/usuarios/{id}` | ADMIN | Ver usuario |
| POST | `/api/usuarios` | ADMIN | Crear usuario con rol |
| PUT | `/api/usuarios/{id}` | ADMIN | Editar usuario |
| DELETE | `/api/usuarios/{id}` | ADMIN | Desactivar usuario |
| GET/POST | `/api/direcciones` | sesión | Mis direcciones / agregar |
| PUT | `/api/direcciones/{id}/seleccionar` | sesión | Elegir dirección |
| DELETE | `/api/direcciones/{id}` | sesión | Eliminar dirección |
| GET | `/api/direcciones/regiones` | sesión | Regiones válidas |
| GET/POST | `/api/tarjetas` | sesión | Mis tarjetas / agregar (solo últimos 4) |
| PUT | `/api/tarjetas/{id}/seleccionar` | sesión | Elegir tarjeta |
| DELETE | `/api/tarjetas/{id}` | sesión | Eliminar tarjeta |

### Catálogo (8082)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/mangas` · `/api/mangas/{id}` | público | Listar / ver manga con tomos |
| GET | `/api/mangas/papelera` | ADMIN | Mangas eliminados |
| POST | `/api/mangas` | ADMIN | Crear manga (con tomo 1) |
| PUT | `/api/mangas/{id}` | ADMIN | Editar datos del manga |
| DELETE | `/api/mangas/{id}` | ADMIN | Enviar a papelera |
| PUT | `/api/mangas/{id}/restaurar` | ADMIN | Restaurar |
| POST | `/api/mangas/{id}/tomos` | ADMIN | Agregar tomo |
| PUT | `/api/mangas/{id}/tomos/{tomo}` | ADMIN | Cambiar precio y stock del tomo |
| DELETE | `/api/mangas/{id}/tomos/{tomo}` | ADMIN | Eliminar tomo |
| GET | `/api/figuras` · `/api/figuras/{id}` | público | Listar / ver figura |
| GET | `/api/figuras/papelera` | ADMIN | Figuras eliminadas |
| POST | `/api/figuras` | ADMIN | Crear figura |
| PUT | `/api/figuras/{id}` | ADMIN | Editar figura |
| PUT | `/api/figuras/{id}/precio-stock` | ADMIN | Cambiar precio y stock |
| DELETE | `/api/figuras/{id}` | ADMIN | Enviar a papelera |
| PUT | `/api/figuras/{id}/restaurar` | ADMIN | Restaurar |
| POST | `/api/figuras/{id}/galeria` | ADMIN | Agregar imagen |
| DELETE | `/api/figuras/{id}/galeria?ruta=...` | ADMIN | Quitar imagen |
| POST | `/internal/ventas/{pedidoId}` | solo Pedidos (clave interna) | Descuenta stock. **No está en el Gateway** |

### Pedidos (8083)

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/pedidos` | sesión + `Idempotency-Key` | Comprar el carrito |
| GET | `/api/pedidos/mios` | sesión | Mis compras |
| GET | `/api/pedidos` | ADMIN | Todas las ventas |
| GET | `/api/pedidos/{id}` | dueño o ADMIN | Detalle |
| PUT | `/api/pedidos/{id}/estado` | ADMIN | CONFIRMADO → ENVIADO → ENTREGADO |

## Cómo se confirma una compra

1. React envía `POST /api/pedidos` con el JWT, un `Idempotency-Key` (UUID) y los productos
   (`tipo`, `productoId`, `tomo`, `cantidad`). **El precio no viaja**: lo pone Catálogo.
2. Pedidos guarda el pedido como `PENDIENTE` y llama a Catálogo por HTTP interno.
3. Catálogo, en una transacción, bloquea las filas, revisa stock, lo descuenta y guarda un comprobante.
4. Pedidos guarda las líneas con el precio de la compra y marca `CONFIRMADO`.

Si se corta la comunicación, el pedido queda `PENDIENTE`; al reintentar con la misma clave, Catálogo
devuelve el mismo comprobante sin volver a descontar stock (**idempotencia**).

## Pruebas

`mvnw verify` ejecuta las pruebas de integración de cada servicio (H2 en memoria): login, rotación del
refresh token, 401/403 por rol, validaciones, CRUD de usuarios, mangas, tomos, figuras, papelera,
direcciones y tarjetas, descuento de stock idempotente y pedido pendiente si Catálogo no responde.

Más detalle en `docs/FRONTEND_A_API.md` (qué función del frontend se reemplaza por cada endpoint).
