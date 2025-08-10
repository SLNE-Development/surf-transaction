package dev.slne.surf.transaction.core.client.netty.listener

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.transaction.core.client.currency.ClientCurrencyBridge
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundRefreshCurrencies
import org.springframework.stereotype.Component

@Component
@Suppress("unused")
class RefreshCurrencyListener(private val clientCurrencyBridge: ClientCurrencyBridge) {

    @SurfNettyPacketHandler
    fun handleRefreshCurrencies(packet: ClientboundRefreshCurrencies) {
        clientCurrencyBridge.updateCurrencies(packet.currencies)
    }
}