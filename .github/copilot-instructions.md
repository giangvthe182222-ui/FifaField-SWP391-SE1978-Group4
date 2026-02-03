# Copilot Instructions for FifaFieldSystem

## Project Overview
- **Type:** Java web application (likely using JSP/Servlets, Ant build)
- **Structure:**
  - `src/`: Java source code (Controllers, DAOs, Models, Services, Utils)
  - `web/`: JSP views, static assets, and web configuration
  - `database/`: SQL scripts for schema and data
  - `lib/`: External JAR dependencies
  - `build.xml`: Ant build script

## Key Architectural Patterns
- **MVC:**
  - Controllers in `src/java/Controller/`
  - Data access in `src/java/DAO/`
  - Models in `src/java/Models/`
  - Views in `web/View/`
- **JSP for UI:** All user-facing pages are JSPs under `web/View/`
- **DAO Pattern:** All database access is through DAO classes (see `src/java/DAO/`)
- **Utils:** Shared utilities (e.g., `DBConnection.java`, `EmailUtil.java`)

## Developer Workflows
- **Build:**
  - Use `build.xml` with Ant (`ant` command) to build/deploy
  - Output goes to `build/` directory
- **Run/Debug:**
  - Deploy `build/` output to a servlet container (e.g., Tomcat)
  - Web root is `web/`
- **Database:**
  - SQL scripts in `database/` (e.g., `FFFDataBase.sql`)
  - Update schema/data via these scripts
- **Dependencies:**
  - Place JARs in `lib/` and reference in Ant build

## Project-Specific Conventions
- **JSP Naming:**
  - Views grouped by feature (e.g., `View/Equipment/`, `View/Auth/`)
  - Use camel case for JSPs (e.g., `AddEquipment.jsp`)
- **Java Packages:**
  - `Controller`, `DAO`, `Models`, `Service`, `Utils` are top-level under `src/java/`
- **Web Config:**
  - `web/WEB-INF/web.xml` for servlet config
  - `web/META-INF/context.xml` for context params
- **Static Assets:**
  - CSS/images in `web/assets/`

## Integration Points
- **Email:**
  - `Utils/EmailUtil.java` handles email sending
- **Database:**
  - All DB access via `Utils/DBConnection.java` and DAOs
- **Authentication:**
  - `Controller/Auth/`, `DAO/AuthDAO.java`, and related JSPs

## Examples
- To add a new feature:
  1. Create Model in `Models/`
  2. Add DAO in `DAO/`
  3. Add Controller in `Controller/`
  4. Add JSP in `web/View/<Feature>/`

- To update DB schema: Edit `database/FFFDataBase.sql` and update DAOs as needed.

## References
- **Build:** `build.xml`
- **Web Config:** `web/WEB-INF/web.xml`, `web/META-INF/context.xml`
- **DB Connection:** `src/java/Utils/DBConnection.java`
- **Email:** `src/java/Utils/EmailUtil.java`

---
For more details, inspect the relevant directories and files as listed above. If any conventions or workflows are unclear, ask for clarification or check with the project maintainers.
