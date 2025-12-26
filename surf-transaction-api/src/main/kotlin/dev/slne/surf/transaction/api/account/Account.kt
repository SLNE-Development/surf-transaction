package dev.slne.surf.transaction.api.account

import dev.slne.surf.transaction.api.account.member.AccountMemberOperations
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.transactional.Transactional
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import net.kyori.adventure.text.Component
import java.util.*

@OptIn(InternalTransactionApi::class)
interface Account : Transactional, AccountMemberOperations {

    val accountId: UUID
    val name: String
    val ownerUuid: UUID
    val defaultAccount: Boolean

    suspend fun asComponent(): Component

    companion object {
        const val MIN_NAME_LENGTH = 3
        const val MAX_NAME_LENGTH = 32

        suspend fun byId(accountId: UUID): Account? =
            AccountService.instance.getAccountByAccountId(accountId)

        suspend fun byName(name: String): Account? = AccountService.instance.getAccountByName(name)

        suspend fun create(
            owner: UUID,
            name: String
        ): AccountCreationResult = AccountService.instance.createAccount(owner, name)
    }

}