package dev.slne.surf.transaction.server.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import org.springframework.stereotype.Service
import java.util.*

@Service
class AccountService(private val accountRepository: AccountRepository) {

    suspend fun addMemberToAccount(
        accountId: UUID,
        executorId: UUID,
        targetId: UUID
    ) = accountRepository.addMemberToAccount(
        accountId = accountId,
        executor = executorId,
        target = targetId
    )

    suspend fun removeMemberFromAccount(
        accountId: UUID,
        executorId: UUID,
        targetId: UUID
    ) = accountRepository.removeMemberFromAccount(
        accountId = accountId,
        executor = executorId,
        target = targetId
    )

    suspend fun createAccount(
        owner: OfflineCloudPlayer,
        name: String,
        defaultAccount: Boolean
    ): AccountCreationResult {
        val name = name.trim().replace(" ", "_")

        if (name.length < 3) {
            return AccountCreationResult.Failure(
                AccountCreationResult.FailureReason.NAME_TOO_SHORT
            )
        }

        if (name.length > 32) {
            return AccountCreationResult.Failure(
                AccountCreationResult.FailureReason.NAME_TOO_LONG
            )
        }

        if (runCatching { UUID.fromString(name) }.getOrNull() != null) {
            return AccountCreationResult.Failure(
                AccountCreationResult.FailureReason.NAME_IS_UUID
            )
        }

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