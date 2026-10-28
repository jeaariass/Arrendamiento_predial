# Despliegue de arriendaya en el VPS

Sigue la arquitectura del servidor (ver `/root/docs/README.md` del VPS): nginx (host) hace
proxy a contenedores Docker en `127.0.0.1:<puerto>`; PostgreSQL es el `db-postgis` compartido,
accesible por la red `db_net`; las imagenes se construyen en Windows y se suben a GHCR, nunca
se construyen en el servidor.

Un solo contenedor (`arriendaya-web`), porque la app es un monolito Spring Boot + Thymeleaf
(no hay frontend/backend separados como en el proyecto SPA).

## 1. Primera vez (una sola vez)

En el VPS:

```bash
# Crea la base "arriendaya" y el rol app_arriendaya con permisos minimos
/srv/scripts/nueva-bd-app.sh arriendaya

# Directorio del stack
mkdir -p /srv/stacks/arriendaya
```

Copiar `deploy/stacks/arriendaya/docker-compose.yml` a `/srv/stacks/arriendaya/docker-compose.yml`.

Copiar `deploy/stacks/arriendaya/.env.example` a `/srv/stacks/arriendaya/.env` (permisos 600) y
completar `DB_USER`/`DB_PASSWORD` con lo que generó `nueva-bd-app.sh`, y `PUERTO` con
el siguiente puerto libre registrado en `/srv/stacks/PUERTOS.md` (el doc dice "proximos
proyectos desde 8102").

Registrar en Portainer el registro GHCR (`jeaariass`, token `read:packages`) si no esta ya.

Agregar el server block de `deploy/nginx/arriendaya.conf.example` a
`/etc/nginx/sites-available/arriendaya.conf` (ajustando dominio y puerto), simlink en
`sites-enabled`, y pedir el certificado:

```bash
ln -s /etc/nginx/sites-available/arriendaya.conf /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
certbot --nginx -d arriendaya.midominio.co
```

DNS: registro A del dominio -> `179.197.81.33` (igual que indicepsa.co en Hostinger).

## 2. Cada nueva version

1. En Windows, con Docker y git limpios: `docker login ghcr.io -u jeaariass` (una vez).
2. `.\scripts\build-and-push.ps1` -> construye y sube `arriendaya-web:<commit7>`.
3. En el VPS: poner `TAG=<commit7>` en `/srv/stacks/arriendaya/.env`.
4. `/srv/scripts/portainer-stack.sh arriendaya --pull`
5. Verificar: semaforo del panel, `docker logs arriendaya-web --tail 30`.
6. Revertir si algo falla: volver a poner el TAG anterior y repetir el paso 4.

## Notas

- Flyway corre las migraciones (`src/main/resources/db/migration`) automaticamente al
  arrancar el contenedor, igual que Alembic en el proyecto SPA.
- Los datos de ejemplo (`DataSeeder`, `ImportadorAirbnbSeeder`, `ReservaDemoSeeder`,
  `AdminSeeder`) tambien corren en el VPS la primera vez que la tabla `inmueble` esta vacia;
  es lo esperado para que el profesor vea la app con datos.
- La app nunca publica en `0.0.0.0`; el `docker-compose.yml` del stack ya la liga solo a
  `127.0.0.1:${PUERTO_HOST}`.
