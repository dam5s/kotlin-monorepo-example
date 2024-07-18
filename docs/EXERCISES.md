# Exercises

You can use IntelliJ IDEA Community edition for these exercises if you do not have access to Intellij IDEA Ultimate.

## Exercise 1. Adding `tryFindGreetingByLanguage`

Create a new service function `tryFindGreetingByLanguage` for the GreetingService,
it accepts an argument `language` of type `string`.

It returns a random greeting matching that language.

### Step details

 - Add a database migration to add a `language` column of type `varchar`,
   it should not allow `null` and default to `'unknown'`
 - Update the existing protobuf specification of the `greetings-service` to add the new function.
   This should not be a new version as it should not be a breaking change.
 - Test-drive the implementation of that new service function in `greetings-server`
 - Don't forget to test the edge case where there is no matching greeting for the given language.
 - Don't forget to refactor existing and new code once you have your tests, and they are passing.

### Tips

 - The format for a migration file name is `V0__change_something_in_schema.sql`,
   for example in our case it could be `V2__add_language_to_greetings.sql`
 - You can find more details on Flyway migrations
   in [this tutorial](https://www.baeldung.com/database-migrations-with-flyway#3-define-first-migration)
 - [Documentation on versioning of gRPC services](https://learn.microsoft.com/en-us/aspnet/core/grpc/versioning)
 - In order to get access to the generated code of the updated `greetings-service`,
   you will want to run `./gradlew protocols:greetings-service:build` 
 - gRPC requests use the builder pattern for creation, e.g.
   ```kotlin
   MyRequest.newBuilder().setMyField(myValue).build()
   ```
 - Executing a SQL query with arguments would look like this
   ```kotlin
   db.tryFindOne("select name from users where email = ?", email) {
       it.getString("name")
   }
   ```

### Wrap-up

At the end of this stage you should be able to start `greetings-server` and correctly query the new rpc function.

This is a good point to make sure the whole test suite is passing and doing a commit.

## Exercise 2. Adding validations to `tryFindGreetingByLanguage`

Now let's add some validations to this new rpc functions.

### Step details

 - Test-drive adding validation of the language value that is passed in
   (e.g. only accept one of: `english`, `french`, `spanish`)

### Tips

 - See `HelloServiceV2` for an example implementation of validations, and `HelloServiceV2Test` for example tests.
 - `greetings-server` will need to depend on `validation-support`, this is done in the `build.gradle.kts` of that project.
 - [Here is the documentation](https://www.jetbrains.com/guide/java/tutorials/working-with-gradle/syncing-and-reloading/)
   on getting IntelliJ to synchronize with the gradle build appropriately

### Wrap-up

At the end of this stage you should be able to query `tryFindGreetingByLanguage` and trigger validation errors.

This is a good point to make sure the whole test suite is passing and doing a commit.

## Exercise 3. Integrating `tryFindGreetingByLanguage` in `hello-server`

We're getting one step closer to making this new feature available.

### Step details

 - Add an optional argument to the `hello` function of `hello-service` version 2.
   The argument is `language`, of type `string`.
 - This change should not require creating a new version of the API.
 - Test-drive the update of the `hello-server` implementation.
 - Don't forget to test cases with validation failures coming from `greetings-server`.

### Tips

 - Don't forget to regenerate the code after changing the proto file `./gradlew protocols:hello-service:build`
 - Do not re-implement validation of the language in this service, it will bubble back up automatically from `greetings-server`
 - Don't forget to test that the underlying `greetings-server` was called with the language that you passed in.
   You may need to change `TestGreetingService` and/or `TestDataGateway` to achieve that.

### Wrap-up

At the end of this stage you should be able to query `hello-server`'s `hello` function with or without `language`
it should also be able to trigger validation exceptions.

This is a good point to make sure the whole test suite is passing and doing a commit.

## Exercise 4. Integrating the `language` argument in the GraphQL API

Finally, expose the added feature in the GraphQL API.

### Step details

 - Test-drive adding an optional `language` argument to the existing `sayHello` function of `HelloQuery`

### Tips

 - Don't forget to test invocation of the endpoint with and without language and/or name.
 - Make sure to assert in some way that the underlying `hello-server` is called with the correct `language` argument

### Wrap-up

Now you should be able to query GraphQL with or without a language argument.
