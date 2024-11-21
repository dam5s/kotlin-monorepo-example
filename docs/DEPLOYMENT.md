# Deployment

First pass at deployment is done manually on a vanilla Azure account
(e.g. leveraging Azure Container Registry instead of Artifactory,
passwords are in environment variables instead of coming from a Vault...).

I am not an infrastructure specialist, this is just a proof of concept that will be fine-tuned and automated by experts.

## Install Azure CLI

 * `brew install azure-cli`

## Create a Virtual Network

 * `ktmonorepo-dev-vnet`
 * Default subnet: 10.0.0.0/24 (will be used by the database)
 * Apps subnet: 10.0.1.0/24

## Create a managed PostgreSQL service

The configuration below is of course for development only.

 * Go to `Azure Database for PostgreSQL flexible servers` and choose `Create`
 * Name `ktmonorepo-dev-greetings-db`
 * Postgres version `16`
 * Workload type `Development`
 * Admin username `**********`
 * Password `***********`
 * Networking: Private VNet integration (use `ktmonorepo-dev-vnet`, `default` subnet)

This will take a while to deploy. Once deployed, there is a `View connection string` that gives credentials
to access the database.

## Create container registry

 * Name `ktmonorepodev`
 * Login locally with `az acr login --name ktmonorepodev`
 * Enable admin user (this should not be done in production systems, not relevant to us since we will use artifactory)

## Create container app job to provision the database

 * `./gradlew databases:provision-db:container`
 * `docker tag io.damo.kotlinmonorepo.provision-db ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.provision-db:latest`
 * `docker push ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.provision-db:latest`

 * Options for container app job on Azure
   * Name `ktmonorepo-dev-job-provision-db`
   * Trigger type `Manual` (we will trigger with Azure CLI)
   * Registry `ktmonorepodev.azurecr.io`
   * Image `io.damo.kotlinmonorepo.provision-db`
   * Tag `latest`
 * While creating this first job, also create the container apps environment
   * Name `ktmonorepo-dev-container-apps-env`
   * Assign the Virtual network `ktmonorepo-dev-vnet`, `apps` subnet.
 
## Provision the initial empty database and user for greetings-db

Run job with correct environment variables:

 * `PGHOST` host of the deployed postgres server
 * `PGUSER` admin user of the deployed postgres server
 * `PGPASSWORD` admin user password of the deployed postgres server
 * `PROVISION_DATABASE` database name to be created 
 * `PROVISION_USER` user to be provisioned for the database
   (currently it is just one user for running migrations and the app, this could be changed to two separate users if need be) 
 * `PROVISION_PASSWORD` password for the provisioned user
 * ```bash
   az containerapp job start \
   -g ktmonorepo-dev \
   -n ktmonorepo-dev-job-provision-db \
   --image ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.provision-db:latest \
   --env-vars \
   'PGHOST=ktmonorepo-dev-greetings-db.postgres.database.azure.com' \
   'PGUSER=microservicesPocAdmin' \
   'PGPASSWORD=someSuperSecretAdminPassword' \
   'PROVISION_DATABASE=greetings_dev' \
   'PROVISION_USER=greetings_user' \
   'PROVISION_PASSWORD=superSecret1!'
   ```

**Warning** in order to pass in the environment variables, you *also* need to pass in the `--image` argument.
If omitted, it will be *silently* ignored.

## Create and run greetings-db migrations job

 * `./gradlew databases:greetings-db:container`
 * `docker tag io.damo.kotlinmonorepo.greetings-db ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.greetings-db:latest`
 * `docker push ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.greetings-db:latest`
 * Options for container app job on Azure
   * Name `ktmonorepo-dev-job-greetings-db`
   * Trigger type `Manual` (we will trigger with Azure CLI)
   * App Environment `ktmonorepo-dev-container-apps-env`
   * Registry `ktmonorepodev.azurecr.io`
   * Image `io.damo.kotlinmonorepo.greetings-db`
   * Tag `latest`
   * Environment variables
     * `FLYWAY_URL=jdbc:postgresql://ktmonorepo-dev-greetings-db.postgres.database.azure.com:5432/greetings_dev`
     * `FLYWAY_USER=greetings_user` (same as `PROVISION_USER` above)
     * `FLYWAY_PASSWORD=superSecret1!` (same as `PROVISION_PASSWORD` above, this probably would be a secret from a vault instead)
   
Once created, run:

 * `az containerapp job start -g ktmonorepo-dev -n ktmonorepo-dev-job-greetings-db`

## Create greetings-server container app

 * `./gradlew applications:greetings-server:container`
 * `docker tag io.damo.kotlinmonorepo.greetings-server ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.greetings-server:latest`
 * `docker push ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.greetings-server:latest`

 * Options for container app on Azure
   * Name `ktmonorepo-dev-greetings-server`
   * App Environment `ktmonorepo-dev-container-apps-env`
   * Registry `ktmonorepodev.azurecr.io`
   * Image `io.damo.kotlinmonorepo.greetings-server`
   * Tag `latest`
   * Environment variables
      * `DATABASE_URL=jdbc:postgresql://ktmonorepo-dev-greetings-db.postgres.database.azure.com:5432/greetings_dev`
      * `DATABASE_USERNAME=greetings_user` (same as `PROVISION_USER` above)
      * `DATABASE_PASSWORD=superSecret1!` (same as `PROVISION_PASSWORD` above, this probably would be a secret from a vault instead)
   * Ingress (we will configure via command line)

```bash
az containerapp ingress enable \
    --resource-group ktmonorepo-dev \
    --name ktmonorepo-dev-greetings-server \
    --transport http2 \
    --target-port 8080 \
    --type internal \
    --allow-insecure true
```

Once the app is created, the liveness probe can be configured on the Azure portal.
 * Transport `HTTP`
 * Path `/health`
 * Port `8081`
 * Initial delay seconds `0`
 * Period seconds `10`

## Create hello-server container app

* `./gradlew applications:hello-server:container`
* `docker tag io.damo.kotlinmonorepo.hello-server ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.hello-server:latest`
* `docker push ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.hello-server:latest`

* Options for container app on Azure
   * Name `ktmonorepo-dev-hello-server`
   * App Environment `ktmonorepo-dev-container-apps-env`
   * Registry `ktmonorepodev.azurecr.io`
   * Image `io.damo.kotlinmonorepo.hello-server`
   * Tag `latest`
   * Environment variables
      * `GREETINGS_HOST=ktmonorepo-dev-greetings-server.internal.redmushroom-9a17f78b.westus.azurecontainerapps.io`
      * `GREETINGS_PORT=80`
   * Ingress (we will configure via command line)

```bash
az containerapp ingress enable \
    --resource-group ktmonorepo-dev \
    --name ktmonorepo-dev-hello-server \
    --transport http2 \
    --target-port 8080 \
    --type internal \
    --allow-insecure true
```

Once the app is created, the liveness probe can be configured on the Azure portal.
* Transport `HTTP`
* Path `/health`
* Port `8081`
* Initial delay seconds `0`
* Period seconds `10`

The liveness probe API is currently not implemented in Typescript.

## Create and run hello-cli job

* `./gradlew applications:hello-cli:container`
* `docker tag io.damo.kotlinmonorepo.hello-cli ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.hello-cli:latest`
* `docker push ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.hello-cli:latest`

* Options for container app job on Azure
    * Name `ktmonorepo-dev-job-hello-cli`
    * Trigger type `Manual` (we will trigger with Azure CLI)
    * App Environment `ktmonorepo-dev-container-apps-env`
    * Registry `ktmonorepodev.azurecr.io`
    * Image `io.damo.kotlinmonorepo.hello-cli`
    * Tag `latest`
    * Environment variables
        * `HELLO_SERVER_HOST=ktmonorepo-dev-hello-server.internal.redmushroom-9a17f78b.westus.azurecontainerapps.io`
        * `HELLO_SERVER_PORT=80`
        * `GREETINGS_DB_URL=jdbc:postgresql://ktmonorepo-dev-greetings-db.postgres.database.azure.com:5432/greetings_dev`
        * `GREETINGS_DB_USERNAME=greetings_user`
        * `GREETINGS_DB_PASSWORD=superSecret1!`

* Run with `az containerapp job start -g ktmonorepo-dev -n ktmonorepo-dev-job-hello-cli`

## Updating a running container app

e.g. for `hello-server`

```
./gradlew :applications:hello-server:container
docker tag io.damo.kotlinmonorepo.hello-server ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.hello-server:latest
docker push ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.hello-server:latest
az containerapp update -g ktmonorepo-dev -n ktmonorepo-dev-hello-server -i ktmonorepodev.azurecr.io/io.damo.kotlinmonorepo.hello-server:latest
```
