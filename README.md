# Task Manager API

Proyecto demo de una API REST para gestión de tareas construida con Spring Boot. Sirve como base de código
para incorporar de forma incremental un pipeline CI/CD DevSecOps con GitHub Actions.

> Nota: este repositorio no incluye workflows de CI/CD todavía. Se irán agregando en sesiones futuras.

## Stack técnico

- Java 21
- Spring Boot 3.3
- Maven
- Spring Web, Spring Data JPA, Spring Validation, Spring Boot Actuator
- H2 (perfiles `dev` / `test`) y PostgreSQL (perfil `prod`)
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito
- Lombok

## Estructura del proyecto

```
src/main/java/com/devsecopsdemo/taskmanager/
├── controller/     # Controladores REST
├── service/        # Lógica de negocio y mapeo DTO <-> entidad
├── repository/      # Repositorios Spring Data JPA
├── model/           # Entidades JPA y enums
├── dto/             # Objetos de transferencia (request/response)
└── exception/        # Excepciones y manejo centralizado de errores
```

## Requisitos previos

- JDK 21
- Maven 3.9+
- Docker y Docker Compose (opcional, para levantar la app con PostgreSQL)

## Build

```bash
mvn clean install
```

## Tests

```bash
mvn test
```

Incluye tests unitarios del servicio (Mockito) y tests de integración de los controladores
(`@WebMvcTest` y `@SpringBootTest` con base de datos H2 en memoria).

## Ejecución local

### Perfil `dev` (H2 en memoria, por defecto)

```bash
mvn spring-boot:run
```

La app queda disponible en `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Consola H2: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:taskmanager`, usuario `sa`, sin password)
- Health check: `http://localhost:8080/actuator/health`

### Perfil `prod` (PostgreSQL)

Requiere una instancia de PostgreSQL accesible. Variables de entorno esperadas:

| Variable      | Descripción            | Default       |
|---------------|-------------------------|---------------|
| `DB_HOST`     | Host de PostgreSQL      | `localhost`   |
| `DB_PORT`     | Puerto de PostgreSQL    | `5432`        |
| `DB_NAME`     | Nombre de la base       | `taskmanager` |
| `DB_USERNAME` | Usuario                 | `taskmanager` |
| `DB_PASSWORD` | Password                | `changeme`    |

```bash
java -jar target/task-manager.jar --spring.profiles.active=prod
```

## Ejecución con Docker

Construir y levantar la app junto con PostgreSQL:

```bash
docker compose up --build
```

La app quedará disponible en `http://localhost:8080` usando el perfil `prod`, conectada al contenedor de PostgreSQL.

Para detener y limpiar los contenedores:

```bash
docker compose down
```

Para eliminar también el volumen de datos de PostgreSQL:

```bash
docker compose down -v
```

## Endpoints principales

| Método | Endpoint             | Descripción                        |
|--------|-----------------------|-------------------------------------|
| GET    | `/api/tasks`          | Lista tareas (paginado)             |
| GET    | `/api/tasks/{id}`     | Obtiene una tarea por id            |
| POST   | `/api/tasks`          | Crea una nueva tarea                |
| PUT    | `/api/tasks/{id}`     | Actualiza una tarea existente       |
| DELETE | `/api/tasks/{id}`     | Elimina una tarea                   |
| GET    | `/actuator/health`    | Health check                        |

### Ejemplo de payload (`POST /api/tasks`)

```json
{
  "title": "Configurar pipeline DevSecOps",
  "description": "Agregar etapas de SAST, SCA y secret scanning",
  "status": "PENDING",
  "dueDate": "2026-08-01"
}
```

## Perfiles de configuración

| Perfil | Base de datos     | Uso                              |
|--------|--------------------|------------------------------------|
| `dev`  | H2 en memoria       | Desarrollo local (perfil por defecto) |
| `test` | H2 en memoria       | Ejecución de tests automatizados   |
| `prod` | PostgreSQL          | Despliegue (config lista, no se levanta automáticamente) |
