package dev.slne.surf.transaction.server.netty.listener

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import dev.slne.surf.transaction.core.netty.packets.CurrencyCreateResultResponsePacket
import dev.slne.surf.transaction.core.netty.packets.ServerboundCreateCurrencyPacket
import dev.slne.surf.transaction.core.netty.packets.ServerboundMakeDefaultCurrencyPacket
import dev.slne.surf.transaction.server.currency.CurrencyService
import org.springframework.stereotype.Component

@Component
@Suppress("unused")
class CurrencyPacketListener(private val currencyService: CurrencyService) {

    @SurfNettyPacketHandler
    suspend fun handleCreateCurrency(packet: ServerboundCreateCurrencyPacket) {
        val result = currencyService.createCurrency(packet.currency)
        packet.respond(CurrencyCreateResultResponsePacket(result))
    }

    @SurfNettyPacketHandler
    suspend fun handleMakeDefaultCurrency(packet: ServerboundMakeDefaultCurrencyPacket) {
        val result = currencyService.makeDefaultCurrency(packet.currency as CurrencyImpl)
        packet.respond(CurrencyCreateResultResponsePacket(result))
    }
}