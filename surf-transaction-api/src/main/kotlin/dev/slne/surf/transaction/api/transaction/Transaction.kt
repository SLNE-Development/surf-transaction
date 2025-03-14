package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.transaction.api.user.TransactionUser
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.*

interface Transaction {

    val identifier: UUID

    val sender: TransactionUser?
    val receiver: TransactionUser?

    val currency: Currency
    val amount: BigDecimal

    val extra: Object2ObjectMap<String, String>

    val createdAt: ZonedDateTime?
    val updatedAt: ZonedDateTime?

}