# LMS handover notes

The original business service under `org.example.lms` has been retained. The infrastructure changes are:

- H2 runtime dependency and H2 console configuration removed; PostgreSQL JDBC is now the application database.
- PostgreSQL connection settings, Redis connection settings, SMTP credentials and JWT signing key are supplied through environment variables.
- Redis-backed Spring Cache added for book details, book lists and active membership plans, with TTLs and mutation-triggered eviction.
- Separate `gateway` Gradle subproject added with Spring Cloud Gateway WebFlux and Spring Cloud LoadBalancer. Docker Compose configures three LMS service instances via static service discovery; Eureka is not required for this fixed local topology.
- OpenAPI/Swagger UI added with JWT Bearer support and domain-specific groups/tags.
- Actuator health/readiness endpoints added.
- Admin initialization now reads Spring's Environment and the process environment, avoids printing secrets, and handles concurrent first-start creation across replicas.
- Scheduled jobs can be toggled with `LMS_SCHEDULING_ENABLED`; Compose runs them on the first replica only to avoid duplicate local cron processing.
- Existing book PATCH update now explicitly saves the modified entity; the previous service method changed a detached entity without a transaction/save operation.

## Known limitations

- The environment used for this handover has Java 21 but no installed Docker CLI and no cached Gradle 9.6.0 distribution. Downloading the Gradle distribution failed because `services.gradle.org` could not be resolved. Therefore a clean Gradle build, application startup, database/cache integration, gateway balancing, Swagger requests and end-to-end business tests were not executed here.
- Hibernate `ddl-auto=update` is retained for development bootstrapping. Production should move to reviewed, versioned Flyway or Liquibase migrations before schema evolution.
- Static service discovery is intended for the supplied Compose topology. Use a real service registry or platform-native discovery for dynamic production instances.
- Compose's one-scheduler-replica setting is not distributed leader election. Use a distributed lock for failover-safe scheduled jobs.
- The source archive contained no test source files. Automated business-flow tests still need to be added and run in an environment with Gradle and Docker.

See `README.md` for startup, environment configuration and API examples.
