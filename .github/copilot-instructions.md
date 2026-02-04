# Copilot instructions — FifaField (SWP391)

Concise, repo-specific guidance to make an AI coding assistant productive immediately.

- **Big picture:** A Jakarta EE (Jakarta Servlet/JSP) web app using an Ant / NetBeans layout. Java source is in `src/java`, views are classic JSPs in `web/View`, and build outputs appear in `build/web` and `dist/`. Persistence is Microsoft SQL Server with simple DAO classes using plain JDBC.

- **Key locations (quick reference):**
  - `src/java/Controller/*` — servlets handling routes (e.g. `Controller.Auth.LoginServlet`).
  - `src/java/DAO/*` — database access (e.g. `AuthDAO.registerCustomer`, `GoogleAuthDAO.findOrCreateUserByGoogle`).
  - `src/java/Utils/DBConnection.java` — DB connection string and `main()` to test connectivity.
  - `src/java/Utils/EmailUtil.java` — SMTP / email sending logic (contains embedded SMTP creds).
  - `web/WEB-INF/web.xml` — servlet declarations & mappings (most routes are declared here).
  - `web/View/*` — JSP pages (scriptlet-style, e.g. `web/View/Auth/login.jsp`).
  - `database/FFFDataBase.sql` — full schema and triggers (note: Role table must contain expected role names).

- **Build, run & debug:**
  - Recommended: open in NetBeans and Run/Deploy to a Tomcat 10+ server (Jakarta namespace).
  - CLI: run `ant` (or `ant dist`) in project root to compile and produce `build/web/` and `dist/FifaField.war`.
  - Java target: Java 8 (see `nbproject/project.properties` - `javac.source=1.8`).
  - Quick DB smoke test: run the `main` in `src/java/Utils/DBConnection.java` to confirm connectivity.

- **Important patterns & conventions:**
  - Plain JDBC with try-with-resources everywhere; multi-step DB changes follow the pattern: setAutoCommit(false) → try { ... commit() } catch { rollback() }.
  - GUIDs are produced by SQL Server: `SELECT CONVERT(VARCHAR(36), NEWID())` (see `AuthDAO.newGuid`).
  - Password length is constrained to NVARCHAR(20). Several DAOs throw SQLException if `password.length() > 20` (see `AuthDAO`).
  - Role names are looked up by string (e.g. `customer`, `staff`); ensure the `Role` table contains these names. Note: one filter checks for `ADMIN` (case-insensitive), so be consistent with role naming.
  - Views use JSP scriptlets and the request/session attribute conventions (Controller sets attributes → JSP reads them directly).

- **Secrets & external integrations (explicit places to check):**
  - DB creds: `src/java/Utils/DBConnection.java` (default: `FifaFieldDB`, user `sa`, password `123`).
  - SMTP creds: `src/java/Utils/EmailUtil.java` (embedded Gmail + app password).
  - Google OAuth: `src/java/Controller/Auth/GoogleLoginServlet.java` contains a `CLIENT_ID` placeholder and `REDIRECT_URI` constant.
  - These are currently embedded in source — rotate to environment/config before production.

- **Common gotchas & troubleshooting pointers:**
  - Container compatibility: requires Tomcat 10+ (Jakarta package names). Using Tomcat 9/older will cause class mismatch errors.
  - If roles are missing (`customer`/`staff`/`ADMIN`) some registration flows will throw errors — check `Role` table seeds.
  - Email send fails if SMTP credentials are revoked or Google blocks the app — check `EmailUtil` and Gmail app password settings.
  - Watch for small package vs folder naming inconsistencies (e.g., `package filter;` vs folder `Filter/`) — NetBeans usually compiles fine, but be cautious when moving files.

- **Files to inspect first for common tasks:**
  - Authentication & user flows: `src/java/Controller/Auth/*`, `src/java/DAO/AuthDAO.java`, `src/java/DAO/GoogleAuthDAO.java`.
  - DB schema and migrations: `database/FFFDataBase.sql` (triggers auto-populate `Location_Equipment` on inserts).
  - Email and OTP: `src/java/Utils/EmailUtil.java`, `src/java/DAO/PasswordResetDAO.java`.
  - UI templates: `web/View/*` (look for scriptlet usage when altering pages).

- **How to add a feature (quick checklist):**
  1. Add servlet under `src/java/Controller/<Area>/` and implement `doGet`/`doPost`.
  2. Register servlet and URL mapping in `web/WEB-INF/web.xml` (this project declares mappings manually).
  3. Add/modify a JSP under `web/View/<Area>/` and static assets under `web/assets/`.
  4. Update DAO(s) under `src/java/DAO/` and the DB seed in `database/FFFDataBase.sql` if schema changes are required.
  5. Build (`ant`) and deploy to Tomcat 10+; run DB `main()` if DB issues appear.

- **Small, safe tasks the agent can perform automatically:**
  - Scan codebase for TODOs, embedded secrets, or missing role seeds.
  - Add unit-style smoke tests that run `DBConnection.main()` or validate servlet URL lists.
  - Generate quick edit+deploy checklist with exact `ant` targets and deploy instructions for Tomcat.

If anything in this file is unclear or you'd like more examples (e.g., exact SQL seed snippet for `Role`, or a sample servlet + JSP pair), tell me which part to expand and I'll iterate. 
