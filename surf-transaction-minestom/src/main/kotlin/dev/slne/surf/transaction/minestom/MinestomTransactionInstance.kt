package dev.slne.surf.transaction.minestom

import com.google.auto.service.AutoService
import dev.slne.surf.api.minestom.coroutine.minestomAsyncScope
import dev.slne.surf.transaction.core.client.ClientTransactionalInstance
import dev.slne.surf.transaction.core.common.TransactionInstance
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

@AutoService(TransactionInstance::class)
class MinestomTransactionInstance : ClientTransactionalInstance() {
    override val dataPath: Path
        get() = TransactionMinestomPlugin.dataDirectory

    override val scope: CoroutineScope
        get() = minestomAsyncScope
}
