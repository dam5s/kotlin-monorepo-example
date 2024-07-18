# Lifecycle of an API and its database

## Requirements

Without specific priority, the following facts should be taken into account when designing an ecosystem of services in front of databases.

* Database will outlive the application(s) and should retain integrity and be usable without the application
* Database changes should not break the contract of an API
* Multiple versions of an API should be able to use the same database schema (v1 & v2 exist in the same service)
* Multiple versions of the service (blue and green) should be able to use the database schema at the same time
* Database changes should be testable before deployment of a new version of the service

## Lifecycle of the database and the service

### Deploying database schema changes

* Initial setup is done by infra
* Database migration is run as its own process, independently of application deployment.
* Impact of database migration on existing traffic can be monitored and verified before potential deployment of an updated service.
* Migrations are never rolled back, instead a new migration is run to resolve potential problems.
* Migrations are never destructive, two different versions of the service should be able to run at the same time.

### Deploying service updates (Blue/Green deployment)

* Once any potential migration has been run and determined to be successful, deploy a new version of the service. This service is not accessible to the public yet.
* Run smoke tests against this new version
* Progressively divert public traffic to the new instances:
    * If the tests have passed, update the DNS entries to point at both the old service and the new one, at this point we have 50% traffic on the old service and 50% on the new.
    * Validate the error rate is not climbing
* Turn off traffic to the old service and have DNS entries only point at the new service.

## Impact on the implementation

* We cannot use the database schema to generate the schema of the API
* We cannot migrate the database on application startup or during the deployment of the service
* We have types in the code of the application that represent:
    * the database content (those might be tied to an ORM if we choose to use one)
    * the public API (those should be used for generating the gRPC code)
* We need to have proper anti-corruption layers at the database level and the gRPC level
