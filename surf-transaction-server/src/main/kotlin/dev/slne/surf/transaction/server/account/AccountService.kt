package dev.slne.surf.transaction.server.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundCreateAccountResponsePacket.AccountCreationResult
import org.springframework.stereotype.Service
import java.util.*

@Service
class AccountService(private val accountRepository: AccountRepository) {

    suspend fun createAccount(
        owner: OfflineCloudPlayer,
        name: String
    ): AccountCreationResult {
        val account = accountRepository.getByAccountName(name)

        if (account != null) {
            return AccountCreationResult.Failure(
                AccountCreationResult.FailureReason.NAME_ALREADY_EXISTS
            )
        }

        return AccountCreationResult.Success(accountRepository.createAccount(owner, name))
    }

    suspend fun getDefaultAccount(player: OfflineCloudPlayer) =
        accountRepository.getDefaultAccount(player)

    suspend fun findAccountByAccountId(accountId: UUID) =
        accountRepository.getByAccountId(accountId)
}