# Copilot Instructions

## Build, test, and validation commands

This repository is a **Gradle Kotlin DSL multi-module build**. Run commands from the repository root with the wrapper:

```powershell
.\gradlew.bat <task>
```

Common commands:

```powershell
.\gradlew.bat :surf-transaction-paper:build
.\gradlew.bat :surf-transaction-paper:test
.\gradlew.bat :surf-transaction-paper:test --tests "dev.slne.surf.transaction.paper.ExampleTest"
.\gradlew.bat :surf-transaction-api:checkKotlinAbi
.\gradlew.bat :surf-transaction-api:updateKotlinAbi
.\gradlew.bat :surf-transaction-api:publishToMavenLocal
```

There is no useful root `build`/`check` lifecycle here; validation is done **per module**. For a full repo pass, run:

```powershell
.\gradlew.bat :surf-transaction-api:check `
  :surf-transaction-core:surf-transaction-core-common:check `
  :surf-transaction-core:surf-transaction-core-client:check `
  :surf-transaction-paper:check `
  :surf-transaction-velocity:check `
  :surf-transaction-microservice:check
```

The repository currently has Gradle `test` tasks for every module, but no committed `src/test` tree yet. When adding tests, run them through the owning module with `--tests`.

## High-level architecture

`surf-transaction` is split into API, shared core, client runtime, platform adapters, and the backing microservice.

- `surf-transaction-api` is the public surface. It exposes the main domain types (`TransactionUser`, `Account`, `Currency`, transaction result types) and companion entry points like `TransactionUser.byUuid(...)`, `Account.byId(...)`, and `Currency.default()`.
- `surf-transaction-core:surf-transaction-core-common` holds shared implementations and transport definitions used by both sides of the system, including `TransactionInstance` and the RabbitMQ protocol packet classes.
- `surf-transaction-core:surf-transaction-core-client` is the client side used by Paper and Velocity. `ClientTransactionalInstance` connects RabbitMQ and Redis, warms the currency cache, and the service implementations (`AccountServiceImpl`, `CurrencyServiceImpl`, `TransactionServiceImpl`) translate API calls into RabbitMQ request/response packets.
- `surf-transaction-microservice` is the system of record. On bootstrap it creates the database schema, registers RabbitMQ request handlers for account/currency/transaction operations, and persists data through Exposed R2DBC repositories.
- `surf-transaction-paper` and `surf-transaction-velocity` are thin platform bootstraps. Both delegate lifecycle to `TransactionInstance`; Paper adds command registration and player-facing Redis event handlers, while Velocity mostly hosts the shared client runtime.

The important runtime flow is: **public API call -> client service implementation -> RabbitMQ request -> microservice handler -> Exposed repository/database**, with **Redis** used alongside that path for cache synchronization and user-facing events.

## Key conventions

- Service discovery is done with `requiredService<T>()` plus `@AutoService(...)`. Follow the existing service-interface pattern instead of wiring manual globals.
- `@InternalTransactionApi` really means internal. The build opts into it inside this repo and excludes those symbols from ABI checks. Keep new external API surface off internal types.
- Account names are normalized at the service boundary with `trim().replace(" ", "_").lowercase()`. Keep new commands, handlers, and lookups consistent with that normalization rule.
- Domain failures are reported with result objects (`TransactionResult`, `AccountCreationResult`, `AccountDeleteResult`, etc.), not by throwing business exceptions. New command and handler logic should branch on those result types.
- Currency cache updates are event-driven. Creating a currency or changing the default currency publishes a Redis event from the client service, and listeners update in-memory client state. If you add a new cross-node state change, wire both the publisher and the subscriber.
- Paper-specific user feedback also travels over Redis (`PaymentReceivedEvent`, admin transaction events). If you change command-side payment/admin flows, check the paired Redis event handlers.
- Command permissions are built hierarchically from parent nodes (`"$PREFIX.balance"`, `"$CURRENCY.admin"`, `"$TRANSACTION_ADMIN.add"`) instead of repeating full permission strings.
