# Contexto del proyecto: arriendaya / Arrendamiento_predial

Proyecto academico de la Maestria en Informatica (Universidad Distrital), materia de
desarrollo de software. Aplicacion web en **Java (Spring Boot) + PostgreSQL + Thymeleaf**
para que propietarios publiquen inmuebles en arriendo por dias y clientes los reserven.
Repo: https://github.com/jeaariass/Arrendamiento_predial

Esta carpeta es un proyecto independiente dentro de `VISUALSTUDIO`, que puede tener varios
proyectos de la maestria (cada uno en su propia carpeta/repo).

## Contexto academico importante (no cambiar sin preguntar)

El profesor dio una regla explicita para el modelo de datos que **ya se siguio** al diseñar
las entidades: *"un objeto en BD debe ser similar a un objeto en la realidad: una persona es
un objeto, un banco es un objeto, un inmueble es un objeto; una reserva es una relacion entre
cliente y propietario."*

Por eso el modelo conceptual (para el informe/diagramas E-R) tiene estos **objetos reales**:
`Persona/Usuario`, `Propietario`, `Cliente`, `Inmueble`, `Banco`, `Ubicacion` (ciudad+pais).
`Reserva`, `Pago` y `Giro` son **relaciones** entre esos objetos, no objetos en si.

Sin embargo, en la **implementacion** (Java/JPA/PostgreSQL), `Reserva`, `Pago` y `Giro` SI se
modelan como sus propias entidades/tablas, porque:
- Necesitan su propia llave primaria (un mismo cliente puede reservar el mismo inmueble varias
  veces en fechas distintas; un pago puede repetirse).
- Tienen atributos propios (fechas, estado, valor) que no le pertenecen a ningun otro objeto.
- Una relacion N:N en JPA siempre requiere una tabla/entidad intermedia al reducirla a tablas.

Esta tension (modelo conceptual "solo objetos" vs. modelo de implementacion con entidades de
relacion) fue discutida y aceptada explicitamente con el usuario; no es un error, es la
decision correcta para este alcance. No reargumentar esto salvo que el usuario lo pida.

Especializacion solapada: un `Usuario` puede ser `Propietario` y `Cliente` al mismo tiempo.
Se implemento con `@OneToOne` + `@MapsId` (llave primaria compartida), no con herencia JPA.

## Stack y estructura

- Java 17, Spring Boot 3.3.4, Maven.
- `spring-boot-starter-web`, `-data-jpa`, `-thymeleaf`, `-validation`, `postgresql`, `lombok`,
  `spring-boot-devtools`, `spring-security-crypto` (solo para BCrypt, no hay Spring Security
  completo: la sesion de login es manual via `HttpSession`).

```
src/main/java/com/arriendaya/app/
  model/        Usuario, Propietario, Cliente, Ubicacion, TipoInmueble (enum), Inmueble,
                Banco, EstadoReserva (enum), Reserva, Pago, Giro
  repository/   interfaces Spring Data JPA (una por entidad); InmuebleRepository tiene
                la query de busqueda con disponibilidad por fechas
  service/      InmuebleService (busqueda/listado), ReservaService (crea reserva, calcula
                valorTotal), UsuarioService (registro, login, conversion de roles)
  controller/   HomeController (/, /buscar), AuthController (/login, /registro, /logout),
                InmuebleController (/inmuebles/{id}, /inmuebles/{id}/reservar)
  config/       SeguridadConfig (bean PasswordEncoder BCrypt), DataSeeder (CommandLineRunner
                que carga datos de ejemplo solo si la tabla inmueble esta vacia)
src/main/resources/
  templates/    index, resultados, detalle, login, registro, reserva_confirmada, error,
                fragments/layout.html (header/footer compartidos)
  static/css/style.css   diseno "tipo Booking.com": hero azul, buscador amarillo superpuesto,
                grid de tarjetas, ficha de reserva fija en el detalle
  application.properties  datasource localhost:5432/arriendo_inmuebles, ddl-auto=update
```

## Estado actual

Implementado y funcional (no probado end-to-end todavia por el usuario):
- Buscar por ciudad/fechas/huespedes, ver resultados, ver detalle, reservar (requiere login).
- Registro eligiendo rol(es): cliente, propietario, o ambos.
- Login/logout por sesion HTTP simple (sin Spring Security, sin roles/autorizacion real).
- Datos de ejemplo automaticos al primer arranque: 4 ciudades, 3 bancos, 1 propietario demo
  (`propietario.demo@arriendo.co` / `demo1234`), 6 inmuebles con fotos de Unsplash.

Pendiente (no implementado aun):
- Pantallas para que el propietario publique/edite sus propios inmuebles (CRUD de Inmueble).
- Pantallas de Pago (el cliente paga una reserva) y Giro (la plataforma le gira al propietario).
- Cualquier logica de "administracion" (el profesor pidio quitar el rol Administrador del
  modelo de datos, pero el enunciado original si menciona administracion de pagos/reservas;
  evaluar con el usuario si hace falta una vista de administracion simple).
- Validaciones mas estrictas (fechas pasadas, solapamiento de reservas sobre el mismo inmueble
  ya se filtra en la busqueda, pero no se revalida al confirmar la reserva).
- Diagramas de casos de uso, clases, interaccion y actividad (se hicieron por separado los
  diagramas E-R en Mermaid/PlantUML, fuera de este repo de codigo).
- Pruebas unitarias/de integracion (no hay ninguna todavia).

## Como correrlo

1. `CREATE DATABASE arriendo_inmuebles;` en PostgreSQL local.
2. Ajustar `spring.datasource.username` / `password` en `application.properties` con las
   credenciales reales del Postgres local del usuario.
3. `mvn spring-boot:run` o correr `ArriendoApplication.java` desde el IDE.
4. `http://localhost:8080`.

Con `spring-boot-devtools` en el classpath, los cambios en plantillas Thymeleaf y CSS se ven
al refrescar el navegador sin reiniciar; los cambios en `.java` reinician la app solos.

## Convenciones a mantener

- Nombres de paquetes, clases, variables, rutas y plantillas en **espanol** (coherente con el
  resto del proyecto y con el curso).
- Sin tildes en identificadores de codigo (siguiendo el estilo ya usado: `Ubicacion`,
  `direccion`, `telefono`, etc.) para evitar problemas de encoding; los textos visibles al
  usuario en las plantillas tampoco llevan tildes por la misma razon histórica del proyecto.
- Mantener el patron `@MapsId` para los roles de Usuario; no migrar a herencia JPA sin
  discutirlo primero, porque fue una decision deliberada para modelar la especializacion
  solapada.
