# Codebase structure

## Applications and Databases

 - Separate because of separate lifecycles.
 - Databases outlive applications
 - Not always a one-to-one mapping between databases and applications
   (e.g. when extracting a service, or joining services)
 - Evolution of services and databases need to be independent
 - See [Service Lifecycle](./SERVICE_LIFECYCLE.md)

## Directory structure

 - `databases`
   - Contains migrations for individual databases and ways to build containers for running jobs on Azure
   - Also contains `provision-db` which builds a container to run a job on Azure
     for provisioning a database and user on a target Postgres server.
 - `protocols`
   - Contains proto files for individual services,
     as well as build configuration for generating Kotlin server and client code.
 - `applications`
   - Contains applications which can be servers, CLIs... any runnable program would fit in this.
   - Applications can depend on `components`
 - `components`
   - Contains support libraries for simplifying building of applications
   - Can also contain individual domain specific libraries that make up the content of an application.
   - Examples of what could be added in the future: `auth0-support`, `user-accounts`...
   - Individual `components` can depend on other `components`, ideally not on `applications` (maybe except for tests)
   - Gradle will prevent cyclic dependencies across `applications` and `components`,
     so it is important to split domain concepts into components.

## The build system

 - [gradle](https://gradle.org) is the tool used for building, it has great support for monorepos.
 - Using `gradle` [protobuf plugin](https://github.com/google/protobuf-gradle-plugin)
   for code generation (targeting `java` and `kotlin`).
 - See `settings.gradle.kts` for monorepo configuration

Running the build of a specific subproject:
 - `./gradlew protocols:greetings-service:build`
 - `./gradlew protocols:hello-service:build`

Using fuzzy matching:
 - `./gradlew pro:gree:bu`
 - `./gradlew pr:he:bu`

## Database migrations

 - Using Flyway, written in SQL (see `databases/greetings-db/migrations` for example)
 - Independent of application runtime and code
 - Always migrate forward (never rollback in production, deploy new migrations instead)
 - Avoid modifying/populating data in migrations, let's create separate jobs for that instead.
 - Load initial local database configuration with `psql < databases/create_local_databases.sql`
 - Migrate all local databases with:`./gradlew migrate`
 - Targeting a specific database: `./gradlew databases:greetings-db:migrate`

Initial cloud provisioning performed running a job with container built in `databases/provision-db`

See [Service Lifecycle](./SERVICE_LIFECYCLE.md) for more on the lifecycle of databases and applications in production.

## Testing

 - Test by starting and querying the actual server.
 - Inject fake dependencies for integrations to prevent exponential test setup
 - Use a real database during tests unless it is a transitive dependency via another server.
 - No mocking (mocks encourage tests that are tightly coupled to the implementation,
   this makes it hard to maintain/refactor code over time).
 - Stub integrations of servers that are direct dependencies.
 - e.g. if `ServerA` depends on `ServerB` depends on `DatabaseB`,
   start `ServerA` and test version of `ServerB` that integrates with a `TestingDatabaseBGateway`
 - See `frontdoor-server`'s `HelloQueryTest` or `hello-server`'s `HelloServiceV2Test` for example

## Deployment in the Cloud

 - Container platform can run "Long lived processes" (servers) and "One-of tasks" (jobs).
 - Migrations run as jobs in containers that run flyway
 - Servers run in containers (using the java runtime)

More details on one way to do a [manual setup in Azure](./DEPLOYMENT.md)
