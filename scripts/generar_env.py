"""Crea el archivo .env a partir de .env.example con claves aleatorias."""
from pathlib import Path
import secrets

root = Path(__file__).resolve().parents[1]
destino = root / ".env"
if destino.exists():
    raise SystemExit("Ya existe .env. No se modificó.")
texto = (root / ".env.example").read_text(encoding="utf-8")
for original in [
    "local-cambiar-clave-interna-32-caracteres",
    "usuarios-local-cambiar",
    "catalogo-local-cambiar",
    "pedidos-local-cambiar",
]:
    texto = texto.replace(original, secrets.token_urlsafe(36))
destino.write_text(texto, encoding="utf-8")
print("Archivo .env creado.")
