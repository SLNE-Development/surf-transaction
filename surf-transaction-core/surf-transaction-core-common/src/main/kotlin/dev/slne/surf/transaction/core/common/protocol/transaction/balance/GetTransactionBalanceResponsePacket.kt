package dev.slne.surf.transaction.core.common.protocol.transaction.balance

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * Balance query result.
 *
 * When [currencyDisabled] is `true`, the request did not bypass the disabled currency and
 * [balance] is zero instead of the real balance.
 */
@Serializable
data class GetTransactionBalanceResponsePacket(
    val balance: @Contextual BigDecimal,
    val currencyDisabled: Boolean = false
) : RabbitResponsePacket()
