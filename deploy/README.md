# Despliegue de arriendaya en el VPS

Sigue la arquitectura del servidor (ver `/srv/docs/README.md` del VPS): nginx (host) hace
proxy a contenedores Docker en `127.0.0.1:<puerto>`; PostgreSQL es el `db-postgis` compartido,
accesible por la red `db_net`; las imagenes se construyen en Windows y se suben a GHCR, nunca
se construyen en el servidor.

Un solo contenedor (`arriendaya-backend`, imagen `arriendaya-api`), porque la app es un
monolito Spring Boot + Thymeleaf (no hay frontend/backend separados como en el proyecto SPA);
se nombra igual que otros proyectos de un solo contenedor del VPS (`-api` / `-backend`).

Puerto asignado: **8103** (el 8102 ya es de otro proyecto).

## 1. Primera vez (una sola vez)

En el VPS:

```bash
# Crea la base "arriendaya" y el rol app_arriendaya con permisos minimos, y escribe
# DB_NAME / DB_USER / DB_PASSWORD directamente en el .env del stack (no hay que copiarlos a mano)
mkdir -p /srv/stacks/arriendaya
/srv/scripts/nueva-bd-app.sh arriendaya
```

Copiar `deploy/stacks/arriendaya/docker-compose.yml` a `/srv/stacks/arriendaya/docker-compose.yml`.

Copiar `deploy/stacks/arriendaya/.env.example` a `/srv/stacks/arriendaya/.env` (permisos 600,
sin pisar lo que ya escribió `nueva-bd-app.sh`) y completar `TAG` y `PUERTO=8103`. `SEED_DEMO_USERS`
y `ADMIN_EMAIL`/`ADMIN_PASSWORD` ya vienen con valores seguros por defecto (ver seccion de
seguridad abajo); ajustarlos solo si de verdad se quiere un admin o los usuarios demo.

Registrar en Portainer el registro GHCR (`jeaariass`, token `read:packages`) si no esta ya.

Agregar el server block de `deploy/nginx/arriendaya.conf.example` a
`/etc/nginx/sites-available/arriendaya.conf` (ajustando el dominio; el puerto ya es 8103),
simlink en `sites-enabled`, y pedir el certificado:

```bash
ln -s /etc/nginx/sites-available/arriendaya.conf /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
certbot --nginx -d arriendaya.midominio.co
```

DNS: registro A del dominio -> `179.197.81.33` (igual que indicepsa.co en Hostinger).

## 2. Cada nueva version

1. En Windows, con Docker y git limpios: `docker login ghcr.io -u jeaariass` (una vez).
2. `.\scripts\build-and-push.ps1` -> construye y sube `arriendaya-api:<commit7>`.
3. En el VPS: poner `TAG=<commit7>` en `/srv/stacks/arriendaya/.env`.
4. `/srv/scripts/portainer-stack.sh arriendaya --pull`
5. Verificar: semaforo del panel, `docker logs arriendaya-backend --tail 30`.
6. Revertir si algo falla: volver a poner el TAG anterior y repetir el paso 4.

## Seguridad: credenciales de demo

La app trae seeders que crean usuarios con clave fija para que el profesor vea datos de
ejemplo (ver `src/main/java/.../config/*Seeder.java`). En un servidor publico eso es un acceso
conocido para cualquiera, asi que:

- `SEED_DEMO_USERS=false` (valor por defecto en `.env.example`) apaga `propietario.demo` y
  `cliente.demo`. Los 500 alojamientos reales de Bogota (Inside Airbnb) se siguen cargando
  igual, son independientes de estos dos usuarios.
- El usuario administrador NO tiene clave por defecto: `AdminSeeder` solo lo crea si
  `ADMIN_EMAIL` y `ADMIN_PASSWORD` estan completos en el `.env`. Si se necesita un admin en el
  VPS, poner una clave fuerte ahi, nunca `admin1234`.

## Notas

- Flyway corre las migraciones (`src/main/resources/db/migration`) automaticamente al
  arrancar el contenedor, igual que Alembic en el proyecto SPA.
- La app nunca publica en `0.0.0.0`; el `docker-compose.yml` del stack ya la liga solo a
  `127.0.0.1:${PUERTO}`.
- Las sesiones de login se guardan en memoria del proceso (no hay Spring Security ni un store
  compartido): si el contenedor se reinicia (deploy, OOM, etc.) todos los que tenian sesion
  quedan deslogueados. Aceptable para el alcance academico; solo tenerlo presente.
