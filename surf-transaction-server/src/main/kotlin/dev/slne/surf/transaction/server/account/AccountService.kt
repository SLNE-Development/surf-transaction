package dev.slne.surf.transaction.server.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import org.springframework.stereotype.Service
import java.util.*

@Service
class AccountService(private val accountRepository: AccountRepository) {

    suspend fun getDefaultAccount(player: OfflineCloudPlayer) =
        accountRepository.getDefaultAccount(player)

    suspend fun findAccountByAccountId(accountId: UUID) =
        accountRepository.getByAccountId(accountId)
}