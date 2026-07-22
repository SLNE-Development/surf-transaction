package dev.slne.surf.transaction.core.client.user

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.api.user.TransactionUserService
import dev.slne.surf.transaction.core.common.user.TransactionUserImpl
import java.util.UUID

@AutoService(TransactionUserService::class)
class TransactionUserServiceImpl : TransactionUserService {
    override fun byUuid(uuid: UUID) = TransactionUserImpl(uuid)
}
