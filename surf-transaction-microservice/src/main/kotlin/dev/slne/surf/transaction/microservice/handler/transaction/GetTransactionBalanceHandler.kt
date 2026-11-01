package dev.slne.surf.transaction.microservice.handler.transaction

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.transaction.balance.GetTransactionBalanceRequestPacket
import dev.slne.surf.transaction.core.common.protocol.transaction.balance.GetTransactionBalanceResponsePacket
import dev.slne.surf.transaction.microservice.db.currency.CurrencyRepository
import dev.slne.surf.transaction.microservice.db.transaction.TransactionRepository
import kotlinx.coroutines.launch
import java.math.BigDecimal

object GetTransactionBalanceHandler {

    @RabbitHandler
    fun handleGetTransactionBalance(request: GetTransactionBalanceRequestPacket) {
        val (accountId, currencyName, ignoreDisabled) = request
        request.launch {
            if (!ignoreDisabled && CurrencyRepository.isCurrencyEnabled(currencyName) == false) {
                request.respond(GetTransactionBalanceResponsePacket(BigDecimal.ZERO, true))
                return@launch
            }

            val balance = TransactionRepository.balanceDecimal(accountId, currencyName)
            request.respond(GetTransactionBalanceResponsePacket(balance))
        }
    }
}