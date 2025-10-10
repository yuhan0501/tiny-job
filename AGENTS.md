# Repository Guidelines

## Project Structure & Module Organization
The Maven root ties together `tiny-job-admin` (Spring Boot executor and REST APIs) and `tiny-job-spring-cloud-adapter` (Zookeeper discovery bridge). Static assets and templates sit in `tiny-job-admin/src/main/resources`. The React console is in `tiny-job-admin-web`, with feature components under `src/components` and Redux state in `src/redux`. Java tests belong in `src/test/java`; integration data or SQL fixtures should live beside them in `src/test/resources`.

## Build, Test, and Development Commands
- `mvn clean package` builds all Maven modules; use `-pl tiny-job-admin` to scope local changes.
- `mvn -pl tiny-job-admin spring-boot:run` starts the admin service with the bundled `application.yml`.
- `npm install && npm run start` in `tiny-job-admin-web` launches the React dev server with hot reload.
- `npm run build` creates production bundles; follow with `npm run copy` to sync assets into the Spring Boot `static/` tree.
- `mvn test` (or `mvn -pl tiny-job-admin test`) runs the JUnit 5 suite and fails on lint/test violations.

## Coding Style & Naming Conventions
Java sources target 1.8; prefer 4-space indentation, Lombok-free POJOs, and package names under `com.tinyjob` (keep module-specific suffixes such as `.admin` or `.adapter`). Align controller endpoints with lowercase-hyphen paths. Front-end code follows Airbnb ESLint and Stylelint rules—run `npm run eslint` or `npm run stylelint` before pushes. Use PascalCase for React components, camelCase for functions, and SCREAMING_SNAKE_CASE for shared constants.

## Testing Guidelines
Favor JUnit 5 + Mockito for unit coverage; structure names as `*Test` mirroring the class under test. Keep deterministic tests by isolating database access with in-memory fixtures in `src/test/resources`. For HTTP integrations, prefer MockMvc or rest template stubs rather than live services. Flag new features with at least one regression test and update docs when behavior changes.

## Commit & Pull Request Guidelines
Commits should be short, imperative statements (`add log trace.` is the current pattern) and scoped to a single concern. Reference issues with `#id` when applicable and avoid rebasing merge commits from protected branches. Pull requests must describe motivation, outline verification steps (`mvn test`, `npm run eslint`, etc.), and include screenshots for UI changes. Request review from module owners and wait for CI green before merging.

## Configuration & Deployment Notes
Environment defaults reside in `tiny-job-admin/src/main/resources/application.yml`; profiles default to `h2`—set `SPRING_PROFILES_ACTIVE=mysql` when you need the RDS datasource. Docker packaging uses the Spotify plugin, so update `imageName` tags before release. Copy the front-end production build into the backend `static/` directory before building containers.
