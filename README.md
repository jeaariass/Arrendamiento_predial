# arriendaya &mdash; Plataforma de arriendo de inmuebles por dias

Proyecto academico (Maestria en Informatica, Universidad Distrital). Java + Spring Boot + PostgreSQL + Thymeleaf.

Repositorio: https://github.com/jeaariass/Arrendamiento_predial

Esta carpeta (`Arrendamiento_predial`) vive dentro de `VISUALSTUDIO`, que puede contener varios proyectos
de la maestria; cada proyecto es su propia carpeta con su propio repositorio git.

## Modelo de datos

Objetos reales: `Usuario` (persona), `Propietario` y `Cliente` (roles opcionales y simultaneos de un `Usuario`,
especializacion solapada), `Inmueble`, `Ubicacion` (ciudad/pais), `Banco`.

`Reserva`, `Pago` y `Giro` son relaciones entre esos objetos que necesitan su propia llave e historial
(un cliente reserva el mismo inmueble varias veces, un pago puede repetirse, etc.), por eso se implementan
como sus propias tablas/entidades JPA aunque conceptualmente sean "relaciones" y no "objetos" segun la
definicion del profesor.

## Requisitos previos

1. **Java 17** instalado (`java -version`).
2. **Maven** (o usar el wrapper si luego se agrega) &mdash; en VS Code, la extension "Extension Pack for Java" ya trae Maven.
3. **PostgreSQL** corriendo localmente. Crear la base de datos:
   ```sql
   CREATE DATABASE arriendo_inmuebles;
   ```
4. Editar `src/main/resources/application.properties` con tu usuario y contrasena reales de PostgreSQL:
   ```
   spring.datasource.username=postgres
   spring.datasource.password=TU_PASSWORD_AQUI
   ```

## Como abrir y correr el proyecto en VS Code

1. Abrir la carpeta `VISUALSTUDIO` completa en VS Code (`File > Open Folder`).
2. Instalar la extension **"Extension Pack for Java"** y **"Spring Boot Extension Pack"** si no las tienes
   (VS Code las sugiere automaticamente al detectar el `pom.xml`).
3. Esperar a que VS Code indexe el proyecto Maven (barra de estado inferior).
4. Correr la aplicacion de alguna de estas formas:
   - Abrir `ArriendoApplication.java` y presionar **Run** (aparece arriba de `public static void main`).
   - O desde una terminal integrada: `mvn spring-boot:run`.
5. Abrir el navegador en **http://localhost:58712** (puerto configurado en `server.port`).

Al primer arranque, la aplicacion carga automaticamente unos datos de ejemplo (ciudades, bancos y 6 inmuebles
con fotos) para que la pagina de inicio no se vea vacia. Esto solo ocurre una vez (si la tabla `inmueble` ya
tiene datos, no se repite).

Cuenta de ejemplo para iniciar sesion como propietario:
- correo: `propietario.demo@arriendo.co`
- contrasena: `demo1234`

## Compartir la misma base de datos con el equipo

Para que todos tengan exactamente los mismos datos (incluyendo los 500 alojamientos reales
de Bogota importados de Inside Airbnb con coordenadas), hay un dump completo en `sql/`:

```powershell
# Windows PowerShell
.\sql\restaurar.ps1 -Usuario postgres
```

```bash
# Git Bash / Linux / Mac
./sql/restaurar.sh postgres
```

Esto crea la base de datos `arriendo_inmuebles` (si no existe) y carga el esquema completo
mas los datos (`sql/arriendo_inmuebles_dump.sql`). Pide la contrasena de Postgres de forma
interactiva, no hay que escribirla en ningun archivo. Despues solo falta ajustar
`spring.datasource.username`/`password` en `application.properties` con las credenciales
locales de cada quien y correr la app normalmente.

Si prefieren no usar el dump, correr la app en una base de datos vacia tambien reproduce los
mismos datos: `DataSeeder` y `ImportadorAirbnbSeeder` cargan el seed de ejemplo y el CSV de
Bogota (`src/main/resources/data/bogota_airbnb.csv`) automaticamente la primera vez.

**Si regeneras el dump** (por ejemplo tras cambiar los datos de prueba), vuelve a correr:
```powershell
pg_dump -U postgres -h localhost -d arriendo_inmuebles --no-owner --no-privileges --column-inserts -f sql/arriendo_inmuebles_dump.sql
```
y borra manualmente las lineas `\restrict` / `\unrestrict` que agrega pg_dump 18 si aparecen
(solo las entiende psql 18+; si algun companero tiene una version mas vieja, falla la restauracion).

## Acceder desde otro dispositivo en la misma red (celular, etc.)

1. Averiguar la IP local del PC que corre la app (ejemplo: `192.168.1.13`):
   ```powershell
   Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -notlike "127.*" -and $_.IPAddress -notlike "169.254.*" }
   ```
2. Abrir el puerto en el firewall de Windows (requiere **PowerShell como Administrador**):
   ```powershell
   New-NetFirewallRule -DisplayName "Arriendo App 58712" -Direction Inbound -Protocol TCP -LocalPort 58712 -Action Allow
   ```
3. Desde el celular, conectado al mismo Wi-Fi, abrir `http://<IP-DEL-PC>:58712` (ej. `http://192.168.1.13:58712`).
4. **Importante:** cerrar el puerto cuando ya no se necesite (la app no tiene HTTPS ni Spring Security completo,
   mejor no dejarlo expuesto permanentemente):
   ```powershell
   Remove-NetFirewallRule -DisplayName "Arriendo App 58712"
   ```

## Estructura del proyecto

```
src/main/java/com/arriendaya/app/
  model/        -> entidades JPA (Usuario, Propietario, Cliente, Inmueble, Ubicacion, Banco, Reserva, Pago, Giro)
  repository/   -> interfaces Spring Data JPA
  service/      -> logica de negocio (busqueda, registro, reservas)
  controller/   -> controladores Spring MVC (paginas Thymeleaf)
  config/       -> configuracion (encriptacion de contrasenas, datos de ejemplo)
src/main/resources/
  templates/    -> vistas Thymeleaf (inicio, resultados, detalle, login, registro)
  static/css/   -> hoja de estilos (diseno tipo Booking.com)
  application.properties
```

## Funcionalidad implementada

- Buscador de alojamientos por ciudad, fechas y numero de huespedes (pagina de inicio estilo Booking).
- Listado de resultados con tarjetas (imagen, tipo, ciudad, precio por noche).
- Pagina de detalle con formulario de reserva (calcula el valor total segun las noches).
- Registro de usuarios eligiendo si quieren ser cliente, propietario, o ambos (especializacion solapada).
- Inicio y cierre de sesion basado en sesion HTTP (sin Spring Security completo, para mantener el alcance
  del proyecto dentro de lo pedido).

## Pendiente para el entregable academico

- Diagramas de casos de uso, clases, interaccion y actividad (se hicieron por separado los diagramas E-R).
- Pantallas de administracion (pagos y giros) si el enunciado las exige explicitamente.
