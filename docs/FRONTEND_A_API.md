# Del localStorage del frontend a la API

Guía para la integración (fase 2). Cada función de `Animate-React/src/utils` que hoy guarda en
`localStorage`/`sessionStorage` tiene su reemplazo en el backend. React debe llamar siempre a
`http://localhost:8080/api` (Gateway) con `credentials: "include"`.

| Archivo del frontend | Función | Endpoint |
|---|---|---|
| `utils/usuarios.js` | `buscarUsuario` (login) | `POST /api/auth/login` |
| | `registrarUsuario` | `POST /api/auth/registro` |
| | `obtenerSesion` / al recargar | `POST /api/auth/refresh` |
| | `cerrarSesion` | `POST /api/auth/logout` |
| | `actualizarUsuario` | `PUT /api/usuarios/me` |
| | `obtenerTodosUsuarios` | `GET /api/usuarios` (ADMIN) |
| `utils/catalogoMangas.js` | `obtenerCatalogoCompleto` | `GET /api/mangas` |
| | `obtenerPapelera` | `GET /api/mangas/papelera` |
| | `agregarManga` | `POST /api/mangas` |
| | `eliminarManga` / `restaurarManga` | `DELETE /api/mangas/{id}` / `PUT /api/mangas/{id}/restaurar` |
| | `agregarTomo` / `eliminarTomo` | `POST` / `DELETE /api/mangas/{id}/tomos/{tomo}` |
| | `cambiarPrecioYStockTomo` | `PUT /api/mangas/{id}/tomos/{tomo}` |
| `utils/catalogoFiguras.js` | `obtenerCatalogoFiguras` | `GET /api/figuras` |
| | `agregarFigura` | `POST /api/figuras` |
| | `eliminarFigura` / `restaurarFigura` | `DELETE /api/figuras/{id}` / `PUT /api/figuras/{id}/restaurar` |
| | `cambiarPrecioYStockFigura` | `PUT /api/figuras/{id}/precio-stock` |
| | `agregarImagenGaleria` / `eliminarImagenGaleria` | `POST` / `DELETE /api/figuras/{id}/galeria` |
| `utils/direcciones.js` | `obtenerDirecciones`, `agregarDireccion`, `eliminarDireccion`, `seleccionarDireccion` | `/api/direcciones` |
| `utils/tarjetas.js` | `obtenerTarjetas`, `agregarTarjeta`, `eliminarTarjeta`, `seleccionarTarjeta` | `/api/tarjetas` |
| `utils/ventas.js` | `registrarVenta` | `POST /api/pedidos` |
| | `obtenerVentasDe` | `GET /api/pedidos/mios` |
| | `obtenerVentas` | `GET /api/pedidos` (ADMIN) |
| `utils/inventario.js` | `descontarStock` | lo hace Catálogo dentro de la compra |

Funciones que ya no hacen falta: `cambiarCorreoVentas`, `cambiarCorreoDirecciones` y
`cambiarCorreoTarjetas` (ahora todo se vincula por id de usuario, no por correo).

## Forma de los datos

- Mangas y figuras llegan con **los mismos nombres de campos** que `data/mangas.js` y `data/figuras.js`
  (`titulo`, `genero`, `volumenes: [{ tomo, precio, stock, img }]`, `imgPortada`, `galeria`, ...).
- Roles: el backend usa `ADMIN` / `USUARIO`; el frontend usaba `"Admin"` / `"Usuario"`.
- Usuario: `{ id, nombre, apellido, correo, rol }`. El campo de contraseña se llama `contrasenia`.
- Compra (`POST /api/pedidos`):

```json
{
  "items": [
    { "tipo": "MANGA", "productoId": 1, "tomo": 2, "cantidad": 1 },
    { "tipo": "FIGURA", "productoId": 3, "tomo": null, "cantidad": 1 }
  ],
  "direccion": "Av. Libertad 123, Depto 4, Viña del Mar, Valparaíso",
  "tarjetaUltimos4": "1234"
}
```

## Diferencias con el comportamiento anterior

- Comprar ahora **requiere iniciar sesión** (antes existía el comprador "Invitado").
- El registro ya no rechaza nombres o apellidos repetidos; solo el **correo** debe ser único.
- Errores: siempre `{ status, mensaje, campos }`; `campos` trae el mensaje de cada input inválido.
