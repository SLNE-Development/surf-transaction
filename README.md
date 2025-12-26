# surf-transaction

**surf-transaction** is a modular transaction system for the Surf ecosystem.
It provides a structured API for managing accounts, currencies, and transactions and is designed for
asynchronous use in plugins and modules.

## Features

* Account-based transaction system
* Support for multiple currencies with configurable scaling
* Deposits, withdrawals, and transfers
* User- and account-centric API design
* Extensible transaction metadata
* Clear separation between public API and internal implementation

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("dev.slne.surf.transaction:surf-transaction-api:<version>")
}
```

## Core Concepts

The API is built around a small set of core abstractions:

* **TransactionUser**
  Entry point for user-scoped transactions and account access.

* **Account**
  Accounts that hold balances in one or more currencies.

* **Currency**
  Defines currencies including symbol, scale, and minimum amount.

* **Transaction**
  Immutable representation of a completed transaction.

## Example

```kotlin
val user = TransactionUser.byUuid(userUuid)

user.deposit(
    amount = BigDecimal("100"),
    currency = Currency.default()
)

val balance = user.balance(Currency.default())
```

All transaction operations return a `TransactionResult`, which can be used to evaluate success or
failure.

## Internal APIs

APIs annotated with `@InternalTransactionApi` are **not part of the public API** and must not be
used.
They may change or be removed at any time without notice.