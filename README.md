# surf-transaction

**surf-transaction** is a modular transaction system for the Surf ecosystem.
It provides a structured API for managing accounts, currencies, and transactions and is designed for
asynchronous use in plugins and modules.

## Features

* Account-based transaction system
* Support for multiple currencies with configurable scaling
* Deposits, withdrawals, and transfers
* Durable pending reservations with explicit commit, rollback, and expiration
* User- and account-centric API design
* Extensible transaction metadata
* Clear separation between public API and internal implementation

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    compileOnly("dev.slne.surf.transaction:surf-transaction-api:<version>")
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
  Immutable snapshot of a transaction and its durable lifecycle state.

## Example

```kotlin
val user = TransactionUser.byUuid(userUuid)

user.deposit(
    amount = BigDecimal("100"),
    currency = Currency.default()
)

val balance = user.balance(Currency.default())
```

Immediate transaction operations return a `TransactionResult`, which can be used to evaluate
success or failure. Durable pending workflows use the dedicated result types described below.

## Pending transactions

Pending transactions coordinate money with an external operation that cannot participate in the
financial database transaction. They are durable, cluster-wide reservations stored by the
microservice. They are not timers or locks held in a Paper or Velocity process.

The lifecycle is `PENDING -> COMMITTED`, `PENDING -> ROLLED_BACK`, or
`PENDING -> EXPIRED`. Transactions remain stored in every terminal state for auditing. Calling
`commit` and `rollback` return a new immutable snapshot; they do not mutate an existing
`Transaction` object. `refresh` returns the current snapshot or `null` if the transaction no longer
exists.

### Convenience API

The common workflow reserves funds, runs an external operation, and commits only after that
operation returns successfully:

```kotlin
val result = transactionUser.withPendingWithdrawal(
    amount = renamePrice,
    currency = currency,
    timeout = 30.seconds,
    rollbackOn = PendingRollbackPolicy.on<ClanRenameRejectedException>()
) {
    clanService.renameClan(clanId, newName)
}

when (result) {
    is PendingExecutionResult.Completed -> {
        // The rename returned normally and the withdrawal is committed.
    }

    is PendingExecutionResult.ReservationFailed -> {
        // The block was never called. Handle insufficient funds or an invalid request.
    }

    is PendingExecutionResult.RolledBack,
    is PendingExecutionResult.RollbackFailed -> {
        // Produced only by the value-based Decision variants described below.
    }

    is PendingExecutionResult.ExternalFailureRolledBack -> {
        // ClanRenameRejectedException proved failure and the reservation was released.
    }

    is PendingExecutionResult.ExternalFailureRollbackFailed -> {
        // Reconcile result.transaction; rollback was not confirmed.
    }

    is PendingExecutionResult.CommitFailed -> {
        // The rename succeeded. Retry/reconcile commit using result.transaction.identifier.
        // Never assume that rollback is safe here.
    }
}
```

`rollbackOn` also accepts a predicate:

```kotlin
rollbackOn = PendingRollbackPolicy { throwable ->
    throwable is ClanRenameRejectedException
}
```

When an external API reports failure as a normal value instead of throwing, use a Decision variant:

```kotlin
val result = transactionUser.withPendingWithdrawalDecision(
    amount = renamePrice,
    currency = currency,
    timeout = 30.seconds
) {
    PendingExecutionDecision.fromBoolean(tryRenameClan())
}

when (result) {
    is PendingExecutionResult.Completed -> {
        // tryRenameClan() returned true and commit succeeded.
    }

    is PendingExecutionResult.RolledBack -> {
        // tryRenameClan() returned false and rollback released the reservation.
    }

    is PendingExecutionResult.RollbackFailed -> {
        // The rename was rejected, but rollback must be reconciled.
    }

    else -> handleOtherExecutionResult(result)
}
```

`PendingExecutionDecision.commit(value)` and `rollback(value)` support arbitrary result types;
`PendingExecutionDecision.from(value) { ... }` maps status objects or sealed results. The automatic
methods pass the stable transaction identifier to the block for correlation, but do not expose a
lifecycle-capable `Transaction`; they perform the selected transition after the block returns. Use
the explicit `begin*` lifecycle when the caller itself must own that transition.

### Explicit lifecycle

Use the low-level API when the external outcome may be uncertain or the transaction must survive a
process restart:

```kotlin
when (val reservation = transactionUser.beginWithdrawal(
    amount = renamePrice,
    currency = currency,
    timeout = 30.seconds,
    additionalData = arrayOf(TransactionData.of("clanId", clanId.toString()))
)) {
    is PendingTransactionResult.Created -> {
        val pending = reservation.transaction

        try {
            clanService.renameClan(clanId, newName)
            when (val commit = pending.commit()) {
                is TransactionCommitResult.Committed,
                is TransactionCommitResult.AlreadyCommitted -> Unit
                else -> reconcileCommit(pending.identifier, commit)
            }
        } catch (rejected: ClanRenameRejectedException) {
            when (val rollback = pending.rollback()) {
                is TransactionRollbackResult.RolledBack,
                is TransactionRollbackResult.AlreadyRolledBack -> Unit
                else -> reconcileRollback(pending.identifier, rollback)
            }
        }
    }

    PendingTransactionResult.SenderInsufficientFunds,
    PendingTransactionResult.ReceiverInsufficientFunds -> showInsufficientFunds()

    else -> handleReservationFailure(reservation)
}
```

The matching convenience methods are `withPendingDeposit`, `withPendingWithdrawal`, and
`withPendingTransfer`. Their low-level counterparts are `beginDeposit`, `beginWithdrawal`, and
`beginTransfer`. `TransactionUser` supplies its UUID and default account automatically; the
inherited `Transactional` methods accept explicit account and initiator arguments. For a pending
transfer, calling `commit` or `rollback` on either returned transaction atomically transitions both
sides. The convenience transfer block receives the sender-side identifier, which identifies the
shared lifecycle without exposing manual commit or rollback. `Transaction.byIdentifier(identifier)`
and `transaction.refresh()` return the current authoritative snapshot or `null` when no transaction
with that identifier exists.

### Balance and concurrency semantics

`balance` is the spendable balance:

* committed deposits and withdrawals contribute normally;
* an active pending negative amount is included immediately and therefore reserves funds;
* a pending positive amount is excluded until commit;
* a pending transfer reserves the sender without crediting the receiver;
* rollback or expiration releases a reservation.

The microservice locks all affected account rows in database ID order before checking balances and
inserting reservations. This serializes competing writers across microservice instances and avoids
transfer deadlocks. Commit and rollback update only rows still in `PENDING`, so retries cannot apply
effects twice or report a failed compare-and-set transition as successful.

### Timeout, exceptions, and cancellation

The default pending lifetime is 62 seconds. Callers may pass any finite, positive Kotlin `Duration`
that represents at least one millisecond.
The microservice derives an absolute expiration timestamp from database time. Expired rows stop
affecting spendable balance immediately, even before the periodic cleanup marks them `EXPIRED`.
A commit after the deadline returns `TransactionCommitResult.Expired`.

The convenience API follows conservative saga rules:

* normal block completion attempts commit;
* only exceptions selected by `rollbackOn` attempt rollback;
* unmatched exceptions are propagated and leave the reservation pending;
* coroutine cancellation and fatal JVM errors are propagated without automatic rollback;
* a commit failure after external success returns `CommitFailed` and never triggers rollback;
* a rollback failure is returned explicitly and is never reported as success.

The new pending and lifecycle requests use RabbitMQ RPC. Reservation RPC failures are thrown to the
caller, including reconstructed exceptions originating in the microservice; in that case the block
is not invoked. A `PendingReservationTimeoutException` retains the locally generated transaction
identifiers because the microservice may already have stored the reservation. If a commit or
rollback RPC fails after the external operation ran, the convenience API retains the identifier and
reports `PendingLifecycleFailure.Thrown` inside `CommitFailed` or a rollback-failure result so that
the final state can be reconciled. Typed lifecycle conflicts such as `Expired` are reported as
`PendingLifecycleFailure.TypedResult`.

This mechanism cannot make an arbitrary external service and the financial database globally
atomic. A persisted financial rollback is also different from a database transaction rollback: it
is a new durable lifecycle state that releases a reservation while retaining the audit record.

## Internal APIs

APIs annotated with `@InternalTransactionApi` are **not part of the public API** and must not be
used.
They may change or be removed at any time without notice.
