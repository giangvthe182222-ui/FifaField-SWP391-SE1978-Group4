# Copilot instructions — FifaField (SWP391)

Concise, repo-specific guidance to make an AI coding assistant productive immediately.

- **Big picture:** A Jakarta EE servlet/JSP web app (Ant/NetBeans layout). Java sources in `src/java`, JSP views in `web/View`, build artifacts and deployable webapp under `build/` and `web/`. The app uses SQL Server and plain JDBC DAOs.

- **Key locations:**
  - `src/java/Controller/*` — servlets (e.g. [LoginServlet](src/java/Controller/Auth/LoginServlet.java#L1)).
  - `src/java/DAO/*` — DAO implementations (e.g. [AuthDAO](src/java/DAO/AuthDAO.java#L1)).
  - `src/java/Utils/DBConnection.java` — DB config & quick `main` to test connectivity ([DBConnection](src/java/Utils/DBConnection.java#L1)).
  - `web/WEB-INF/web.xml` — servlet URL mappings and filters ([web.xml](web/WEB-INF/web.xml#L1)).
  - `web/View/*` — JSP pages and layout fragments (Header/Footer in `web/View/Layout`).
  - `database/FFFDataBase.sql` — schema + seed data.

- **Build & run (practical):**
  - Prefer NetBeans for edit+run; alternatively run Ant using the root `build.xml` (`ant` or inspect the file for available targets) and deploy the generated WAR or `web/` folder to Tomcat 10+ (must support `jakarta.servlet`).
  - To quickly verify DB connectivity, run the `main` in `src/java/Utils/DBConnection.java`.

- **Project-specific patterns & gotchas:**
  - DAOs use plain JDBC with try-with-resources. For multi-step operations some DAOs use explicit transactions (see `AuthDAO.registerCustomer`).
  - GUIDs are generated via SQL Server `NEWID()` in DB/DAOs.
  - Password columns are `NVARCHAR(20)` — application enforces a 20-char max; changing this requires schema migration and updates across auth code.
  - Role strings (e.g. `customer`) are assumed present in the `Role` table; DAOs look up role IDs by name.
  - Default DB credentials are embedded in `DBConnection` (`FifaFieldDB`, `sa`, `123`) — update for local development.

- **How to add/modify functionality:**
  - Add a servlet: create under `src/java/Controller/<Area>/`, implement doGet/doPost, then register mapping in `web/WEB-INF/web.xml` and create/modify the corresponding JSP under `web/View/<Area>/`.
  - For DB changes: update `database/FFFDataBase.sql` and corresponding DAO SQL; follow existing try-with-resources + transaction patterns.
  - Follow existing flow: Controller reads request params → call DAO → set request/session attributes → forward or `sendRedirect`.

- **Integration & runtime notes:**
  - Uses Jakarta EE APIs (jakarta.servlet.*) — ensure container compatibility (Tomcat 10+).
  - Libraries/jars live in `lib/` and `web/WEB-INF/lib/` — NetBeans `nbproject` and `build.xml` manage classpath.

- **Files to inspect first when troubleshooting:**
  - [src/java/Controller/Auth/LoginServlet.java](src/java/Controller/Auth/LoginServlet.java#L1) — login/session usage.
  - [src/java/DAO/AuthDAO.java](src/java/DAO/AuthDAO.java#L1) — registration, password rules.
  - [src/java/Utils/DBConnection.java](src/java/Utils/DBConnection.java#L1) — connection strings.
  - [web/WEB-INF/web.xml](web/WEB-INF/web.xml#L1) — mappings and filters.

- **What *not* to change without care:**
  - DB schema (column sizes/types) without updating DAO validation and seeds.
  - Authentication flows and role names — changes must be propagated to SQL seeds and DAOs.

- **If you'd like next steps:**
  - I can run a quick scan for TODOs in DAOs/servlets, or add an edit+deploy checklist with exact `ant` targets — tell me which.

Please tell me which follow-up you'd prefer.
