package dev.slne.surf.transaction.fallback.transaction

import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.transaction.TransactionData
import dev.slne.surf.transaction.fallback.currency.FallbackCurrencyTable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.LongIdTable
import java.util.*

object FallbackTransactionTable : LongIdTable("transaction_transactions") {

    val identifier = char("identifier", 36).transform({ UUID.fromString(it) }, { it.toString() })

    val sender = char("sender", 36).nullable().transform({
        TransactionUser.get(UUID.fromString(it))
    }, {
        it?.uuid.toString()
    })

    val receiver = char("receiver", 36).nullable().transform({
        TransactionUser.get(UUID.fromString(it))
    }, {
        it?.uuid.toString()
    })

    val currency = reference("currency", FallbackCurrencyTable)
    val amount = decimal("amount", 20, 10)

    val extra = largeText("extra")
        .transform({ Json.decodeFromString<TransactionData>(it) }, { Json.encodeToString(it) })
}