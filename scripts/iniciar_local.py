"""Inicia los 4 JAR ya compilados (sin Docker). Requiere Python 3.10+ y Java 17+.

Uso:  python scripts/iniciar_local.py            (perfil h2, no necesita MySQL)
      python scripts/iniciar_local.py --perfil mysql
"""
import argparse
import os
import subprocess
import sys
import time
from pathlib import Path

root = Path(__file__).resolve().parents[1]
run = root / ".run"
run.mkdir(exist_ok=True)

env = os.environ.copy()
archivo_env = root / ".env"
if archivo_env.exists():
    for linea in archivo_env.read_text(encoding="utf-8").splitlines():
        if linea.strip() and not linea.startswith("#") and "=" in linea:
            clave, valor = linea.split("=", 1)
            env.setdefault(clave.strip(), valor.strip())

parser = argparse.ArgumentParser(description="Iniciar backend Animate con H2 o MySQL")
parser.add_argument("--perfil", choices=["h2", "mysql"], default="h2")
args = parser.parse_args()

# (módulo, nombre corto usado en variables y bases)
modulos = [
    ("usuarios-service", "usuarios"),
    ("catalogo-service", "catalogo"),
    ("pedidos-service", "pedidos"),
    ("api-gateway", "gateway"),
]
procesos = []
codigo = 0
try:
    for modulo, corto in modulos:
        jar = root / modulo / "target" / f"{modulo}-1.0.0.jar"
        if not jar.exists():
            raise RuntimeError("Primero compila el backend: mvnw verify (o mvnw package -DskipTests).")
        carpeta = run / corto
        carpeta.mkdir(exist_ok=True)
        log = (carpeta / "app.log").open("w", encoding="utf-8")
        env_servicio = env.copy()
        if corto != "gateway":
            env_servicio["SPRING_PROFILES_ACTIVE"] = args.perfil
            if args.perfil == "mysql":
                mayus = corto.upper()
                env_servicio["DB_URL"] = env.get(
                    f"DB_{mayus}_URL",
                    f"jdbc:mysql://localhost:3306/animate_{corto}?useSSL=false"
                    "&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
                )
                env_servicio["DB_USER"] = env.get(f"DB_{mayus}_USER", f"animate_{corto}")
                env_servicio["DB_PASSWORD"] = env[f"DB_{mayus}_PASSWORD"]
        hijo = subprocess.Popen(
            ["java", "-jar", str(jar)], cwd=carpeta, env=env_servicio, stdout=log, stderr=subprocess.STDOUT
        )
        procesos.append((hijo, log))
        print(f"Iniciando {modulo}. Log: {carpeta / 'app.log'}")
        if corto == "usuarios":
            time.sleep(8)  # Catálogo y Pedidos leen la clave pública de Usuarios
    print("Gateway: http://localhost:8080 | Swagger: http://localhost:8080/swagger-ui.html")
    print("Espera a que los 4 logs digan 'Started'. Ctrl+C detiene todo.")
    while True:
        for hijo, _ in procesos:
            if hijo.poll() is not None:
                raise RuntimeError("Un servicio se detuvo. Revisa los logs en la carpeta .run")
        time.sleep(1)
except KeyboardInterrupt:
    print("Deteniendo servicios...")
except Exception as error:
    print(error, file=sys.stderr)
    codigo = 1
finally:
    for hijo, _ in procesos:
        hijo.terminate()
    for hijo, log in procesos:
        try:
            hijo.wait(timeout=8)
        except subprocess.TimeoutExpired:
            hijo.kill()
        log.close()

raise SystemExit(codigo)
