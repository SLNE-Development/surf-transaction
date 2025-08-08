package dev.slne.surf.transaction.server.account

import dev.slne.surf.transaction.api.account.InternalAccountBridge
import org.springframework.stereotype.Component
import java.util.*

@Component
class ServerAccountBridge(private val accountService: AccountService) : InternalAccountBridge {
    override suspend fun getAccountByAccountId(accountId: UUID) =
        accountService.findAccountByAccountId(accountId)
}