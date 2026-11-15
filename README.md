# Servidor MCP de Gestión de Tareas (`demomcp`)

Servidor **Model Context Protocol (MCP)** desarrollado en **Java 21** con **Spring Boot 3.4.7**, **Spring AI MCP Server** y persistencia en **MySQL 8**. Expone 9 herramientas (`@Tool`) sobre transporte **Streamable HTTP** (`http://localhost:8088/mcp`) para administrar tareas pendientes y completadas desde clientes de inteligencia artificial como **Claude Desktop** y **Google ADK**.

---

## Arquitectura

- **Protocolo:** JSON-RPC 2.0 sobre Streamable HTTP (`/mcp`, puerto `8088`).
- **Persistencia:** Spring Data JPA / Hibernate sobre el esquema MySQL `demomcp` (tabla `tarea`).
- **Validaciones:** Control de concurrencia optimista (`@Version`) y validación estricta de fechas ISO-8601 (`yyyy-MM-dd` con `ResolverStyle.STRICT`).

---

## Herramientas MCP Expuestas (9 Tools)

| Herramienta | Descripción |
| :--- | :--- |
| `listarTodas` | Lista todas las tareas registradas en la base de datos. |
| `listPendientes` | Filtra las tareas con estado `completado = false`. |
| `listarCompletadas` | Filtra las tareas con estado `completado = true`. |
| `contarTareas` | Devuelve el conteo total de registros (`SELECT COUNT(*)`). |
| `buscarPorTitulo` | Busca tareas por coincidencia parcial en el título. |
| `crearTarea` | Crea una tarea nueva validando formato de fecha ISO `yyyy-MM-dd`. |
| `updateTarea` | Actualiza el título o fecha de una tarea existente. |
| `marcarCompletado` | Marca una tarea como completada (`completado = true`). |
| `eliminarTarea` | Elimina una tarea por su identificador numérico. |

---

## Requisitos Previos

- **Java JDK 21** (con la variable de entorno `JAVA_HOME` configurada y agregada al `PATH`).
- **MySQL Server 8.0+** activo en `localhost:3306` con usuario `root` (o un usuario con permisos de creación y escritura en el esquema `demomcp`).
- **Variable de entorno `PWD_UDEA`** definida en el sistema o sesión de terminal con la contraseña de acceso a MySQL.
- **Puerto `8088` libre** en el equipo local (`localhost:8088`).
- **Acceso a Internet** (en la primera ejecución) para que Maven descargue las dependencias de Spring Boot y Spring AI (`1.1.4`).
- **Maven Wrapper** incluido en el repositorio (`.\mvnw.cmd` en Windows o `./mvnw` en Linux/macOS), sin necesidad de tener Maven instalado globalmente.
- **Cliente MCP compatible** para consumir el servidor (opcional, según el escenario de prueba):
  - **Node.js 18+ y `npx`** (requeridos si se conecta mediante **MCP Inspector** o el puente `mcp-remote` en **Claude Desktop**).
  - **Agente cliente en Google ADK (`agentemcp`)** ejecutándose en el puerto `8080`.

---

## Configuración y Ejecución

1. **Clonar el repositorio:**
   ```powershell
   git clone [https://github.com/BryanMedrano/demomcp.git](https://github.com/BryanMedrano/demomcp.git)
   cd demomcp
   ```

2. **Crear la base de datos en MySQL:**
   ```sql
   CREATE DATABASE IF NOT EXISTS demomcp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   *(Spring Boot / Hibernate creará y actualizará automáticamente la tabla `tarea` al iniciar la aplicación).*

3. **Configurar la variable de entorno con la contraseña de MySQL:**
   ```powershell
   $env:PWD_UDEA = "TU_CONTRASENA_MYSQL"
   ```

4. **Ejecutar pruebas unitarias y de contexto (usa base de datos H2 en memoria):**
   ```powershell
   .\mvnw.cmd test
   ```

5. **Iniciar el servidor MCP:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   El servidor quedará disponible en `http://localhost:8088/mcp` mostrando en consola `Registered tools: 9`.

---
