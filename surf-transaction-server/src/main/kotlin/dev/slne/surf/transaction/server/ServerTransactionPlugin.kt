package dev.slne.surf.transaction.server

import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.plugin.utils.context
import dev.slne.surf.transaction.server.currency.CurrencyService
import org.springframework.beans.factory.getBean

class ServerTransactionPlugin : StandalonePlugin() {
    override suspend fun load() {
        context.getBean<CurrencyService>().cacheCurrencies()
    }

    override suspend fun enable() {
    }

    override suspend fun disable() {
    }
}