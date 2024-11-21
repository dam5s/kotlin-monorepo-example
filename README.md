# Kotlin Mono Repo

## Dev Environment

 * JDK 21 (`brew install temurin@21`)
 * PostgresQL 16 (`brew install postgresql@16`)
 * Docker (for building containers, e.g. during `./gradlew build`, not used for running apps locally)

## Configuring application secrets

Copy `gradle.properties.example` as `gradle.properties` and replace the `<secret>` values with their actual values.

## Local database setup

Make sure your local Postgres is running
```bash
brew services start postgresql@16
```

Now configure the database schema
```bash
psql postgres < databases/create_local_databases.sql
./gradlew migrate
```

Note, `psql` might not be on your path, you can have homebrew link it with:
```
brew link postgresl@16 --force
```

## Running the build

Ensure docker is running before running the build.

```bash
./gradlew build
```

If you have this running and passing, you are all setup for local development.

If you want to run the build without building the containers to get quicker feedback,
you can exclude the `container` tasks.

```bash
./gradlew build -x container
```

## Running the migration container

When we deploy to Azure, the migrations are executed thanks to the container that we build. If you want to test it out,
you can build the container with
```
./gradlew databases:greetings-db:container
```

Then you could try and run it with
```bash
docker run \
    -e "FLYWAY_URL=jdbc:postgresql://my-database-host:5432/hello_staging" \
    -e "FLYWAY_USER=db-user-staging" \
    -e "FLYWAY_PASSWORD=superSecret1!" \
    io.damo.kotlinmonorepo.hello-db
```

Locally we don't need to do that, we just use `./gradlew migrate`

## Service lifecycle

The service and the database lifecycle is detailed [in this document](./docs/SERVICE_LIFECYCLE.md)

## Information on API versioning

Microsoft has [good documentation on the topic](https://learn.microsoft.com/en-us/aspnet/core/grpc/versioning)

## Learning to use IntelliJ IDEA

When you first open the project folder in IntelliJ IDEA
it should automatically synchronize with the gradle build configuration.

It is highly recommended to learn the basics of the IDE through the built-in tutorial.
It is available in the `Help` menu, `Learn IDE Features`.
Make sure to select the `Kotlin` version in the dropdown top of the sidebar titled `Learn`.

Jetbrains also has some
[extensive tutorials](https://www.jetbrains.com/guide/java/tutorials/working-with-gradle/introduction/)
on working with Gradle based projects.

Hopefully these help getting familiar with your development environment.
