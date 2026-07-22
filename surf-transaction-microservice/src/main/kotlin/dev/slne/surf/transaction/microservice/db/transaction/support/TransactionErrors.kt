package dev.slne.surf.transaction.microservice.db.transaction.support

import dev.slne.surf.api.core.util.SerializableError
import dev.slne.surf.api.core.util.toSerializableError
import dev.slne.surf.database.libs.io.r2dbc.spi.R2dbcDataIntegrityViolationException
import java.util.*

internal object TransactionErrors {

    fun invalidTransferPair() = SerializableError(
        "INVALID_TRANSFER_DATA",
        "Transfer sides must use distinct identifiers, the same initiator and currency, " +
                "opposite non-zero amounts, and mirrored accounts"
    )

    fun unknownAccountOrCurrency() = SerializableError(
        "ACCOUNT_OR_CURRENCY_NOT_FOUND",
        "An account or currency referenced by the transaction does not exist"
    )

    fun identifierConflict(identifier: UUID) = SerializableError(
        "TRANSACTION_IDENTIFIER_CONFLICT",
        "Transaction identifier $identifier already belongs to different data or state"
    )

    fun constraintViolation(violation: R2dbcDataIntegrityViolationException) = SerializableError(
        "TRANSACTION_CONSTRAINT_VIOLATION",
        "The transaction violates a database constraint${violation.sqlStateSuffix()}",
        violation.toSerializableError()
    )

    fun unexpected(cause: Exception) = SerializableError(
        "DATABASE_ERROR",
        cause.message ?: cause::class.qualifiedName.orEmpty()
    )

    private fun R2dbcDataIntegrityViolationException.sqlStateSuffix(): String =
        sqlState?.takeIf(String::isNotBlank)?.let { " (SQL state $it)" }.orEmpty()
}
