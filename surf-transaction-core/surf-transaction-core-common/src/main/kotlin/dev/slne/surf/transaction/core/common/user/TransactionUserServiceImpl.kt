package dev.slne.surf.transaction.core.common.user

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.api.user.TransactionUserService
import java.util.*

@AutoService(TransactionUserService::class)
class TransactionUserServiceImpl : TransactionUserService {
    override fun byUuid(uuid: UUID) = TransactionUserImpl(uuid)
}