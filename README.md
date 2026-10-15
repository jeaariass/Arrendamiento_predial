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
5. Abrir el navegador en **http://localhost:8080**.

Al primer arranque, la aplicacion carga automaticamente unos datos de ejemplo (ciudades, bancos y 6 inmuebles
con fotos) para que la pagina de inicio no se vea vacia. Esto solo ocurre una vez (si la tabla `inmueble` ya
tiene datos, no se repite).

Cuenta de ejemplo para iniciar sesion como propietario:
- correo: `propietario.demo@arriendo.co`
- contrasena: `demo1234`

## Estructura del proyecto

```
src/main/java/com/ctglobal/arriendo/
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
