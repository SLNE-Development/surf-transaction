package dev.slne.surf.transaction.core.common.user

import dev.slne.surf.transaction.api.account.AccountAccess
import dev.slne.surf.transaction.api.transactional.Transactional
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.common.account.AccountAccessImpl
import dev.slne.surf.transaction.core.common.transactional.TransactionalImpl
import java.util.*

class TransactionUserImpl(
    userUuid: UUID
) : TransactionUser,
    AccountAccess by AccountAccessImpl(userUuid),
    Transactional by TransactionalImpl()