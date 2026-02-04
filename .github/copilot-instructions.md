# Copilot instructions — FifaField (SWP391)

This repository is a Jakarta EE (servlet/JSP) web app using Ant/NetBeans structure. Insert concise, actionable guidance below so an AI coding assistant can be productive immediately.

- **Project layout**: source lives under `src/java` with packages mirrored as folders:
  - `src/java/Controller/*` — servlet controllers (e.g. [LoginServlet](src/java/Controller/Auth/LoginServlet.java#L1)).
  - `src/java/DAO/*` — database access objects (e.g. [AuthDAO](src/java/DAO/AuthDAO.java#L1)).
  - `src/java/Models/*` — domain models.
  - `src/java/Utils/*` — helpers, including [DBConnection](src/java/Utils/DBConnection.java#L1).
  - Views are JSPs under `web/View/*` and the deployed webapp is in `web/`.

- **Build / run**:
  - This is an Ant / NetBeans project. Use the IDE or run the default Ant targets via the provided `build.xml` at repository root. See [build.xml](build.xml#L1).
  - Deployment expects a Jakarta EE servlet container that supports `jakarta.servlet` imports (Tomcat 10+, Jetty with Jakarta support, or a compatible app server).

- **Database**:
  - SQL Server is used. Connection config is in [src/java/Utils/DBConnection.java](src/java/Utils/DBConnection.java#L1). Default URL/user/password are embedded (`FifaFieldDB`, `sa`, `123`) — update for local dev.
  - Schema and seed SQL are in `database/FFFDataBase.sql`.
  - DAOs use plain JDBC with try-with-resources and explicit transactions for multi-step inserts (see `AuthDAO.registerCustomer`).

- **Routing & servlets**:
  - URL → servlet mappings are defined in `web/WEB-INF/web.xml` (e.g. `/login` → `Controller.Auth.LoginServlet`). Inspect that file for endpoints.
  - Many servlets forward to JSPs under `web/View/*` — follow the pattern: Controller reads request params → uses DAO → sets request/session attributes → forwards or redirects.

- **Conventions & gotchas discovered**:
  - Passwords stored in DB as NVARCHAR(20). Code enforces a 20-char max in `AuthDAO.registerCustomer` — do not change client-side validation without migrating DB.
  - GUIDs are generated using SQL Server `NEWID()` via DAO helper methods.
  - Role names like `customer` are expected to exist in `Role` table; DAOs fetch role ids by name.
  - Plaintext DB credentials and plaintext passwords are present — when editing auth flows, preserve compatibility or update schema and all call-sites.

- **Where to make common changes**:
  - Add new endpoints: create a servlet under `src/java/Controller/*`, register it in `web/WEB-INF/web.xml`, and add a JSP under `web/View/*`.
  - DB changes: update `database/FFFDataBase.sql`, then update DAOs accordingly. Follow existing try-with-resources and explicit transaction patterns.
  - UI changes: edit JSPs in `web/View/*` and CSS in `web/assets/css`.

- **Dependencies & libraries**:
  - Project jars live in `lib/` and `web/WEB-INF/lib/` (check `nbproject` and `build.xml` for classpath configuration).

- **Testing & debugging notes**:
  - No automated test suite present — use local Tomcat and the DB to test flows.
  - To quickly verify DB connectivity, run `main` in `src/java/Utils/DBConnection.java`.

- **When editing code, follow these concrete patterns**:
  - Use DAO classes for all DB access; keep SQL in DAO methods.
  - Use `request.getSession(true).setAttribute("userId", ...)` to set logged-in user (see `LoginServlet`).
  - Use `response.sendRedirect(request.getContextPath() + "/View/...jsp")` for post-POST redirects when the project uses redirects.

If anything here is unclear or you'd like more examples (e.g., typical edit+deploy cycle, or mapping of a specific endpoint), tell me which area to expand and I'll iterate.
