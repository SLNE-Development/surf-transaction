package dev.slne.surf.transaction.core.user

import dev.slne.surf.transaction.api.account.AccountAccess
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.account.AccountAccessImpl
import java.util.*

class TransactionUserImpl(
    userUuid: UUID
) : TransactionUser,
    AccountAccess by AccountAccessImpl(userUuid)