package dev.slne.surf.transaction.server.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.account.AccountCreationResult
import org.springframework.stereotype.Service
import java.util.*

@Service
class AccountService(private val accountRepository: AccountRepository) {

    suspend fun createAccount(
        owner: OfflineCloudPlayer,
        name: String,
        defaultAccount: Boolean
    ): AccountCreationResult {
        val account = accountRepository.getByAccountName(name)

        if (account != null) {
            return AccountCreationResult.Failure(
                AccountCreationResult.FailureReason.NAME_ALREADY_EXISTS
            )
        }

        return AccountCreationResult.Success(
            accountRepository.createAccount(
                owner,
                name,
                defaultAccount
            )
        )
    }

    suspend fun deleteAccount(accountId: UUID) = accountRepository.deleteAccount(accountId)

    suspend fun getAccountByName(name: String) =
        accountRepository.getByAccountName(name)

    suspend fun getAllAccountsByOwner(owner: OfflineCloudPlayer) =
        accountRepository.getAllAccountsByOwner(owner)

    suspend fun getDefaultAccount(player: OfflineCloudPlayer) =
        accountRepository.getDefaultAccount(player)

    suspend fun findAccountByAccountId(accountId: UUID) =
        accountRepository.getByAccountId(accountId)
}