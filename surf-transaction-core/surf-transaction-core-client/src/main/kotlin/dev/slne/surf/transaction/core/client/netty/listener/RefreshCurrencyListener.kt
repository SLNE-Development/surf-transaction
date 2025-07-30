package dev.slne.surf.transaction.core.client.netty.listener

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.transaction.core.client.currency.ClientCurrencyBridge
import dev.slne.surf.transaction.core.netty.packets.ClientboundRefreshCurrencies
import org.springframework.stereotype.Component

@Component
class RefreshCurrencyListener(private val clientCurrencyBridge: ClientCurrencyBridge) {

    @SurfNettyPacketHandler
    fun handleRefreshCurrencies(packet: ClientboundRefreshCurrencies) {
        clientCurrencyBridge.updateCurrencies(packet.currencies)
        throw RuntimeException("Currencies refreshed: ${packet.currencies.size} currencies")
    }
}