package dev.slne.surf.transaction.server.netty.sync

import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.plugin.spring.task.CloudInitialSynchronizeTask
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundRefreshCurrencies
import dev.slne.surf.transaction.server.currency.CurrencyService
import org.springframework.stereotype.Component as SpringComponent

@SpringComponent
class SyncCurrenciesTasks(private val currencyServiceImpl: CurrencyService) :
    CloudInitialSynchronizeTask {
    override suspend fun execute(client: NettyClient) {
        client.fireAndForget(ClientboundRefreshCurrencies(currencyServiceImpl.currencies))
    }
}