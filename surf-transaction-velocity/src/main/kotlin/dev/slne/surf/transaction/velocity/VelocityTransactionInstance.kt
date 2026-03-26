package dev.slne.surf.transaction.velocity

import com.github.shynixn.mccoroutine.velocity.scope
import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.client.ClientTransactionalInstance
import dev.slne.surf.transaction.core.common.TransactionInstance
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

@AutoService(TransactionInstance::class)
class VelocityTransactionInstance : ClientTransactionalInstance() {
    override val dataPath: Path
        get() = plugin.dataPath

    override val scope: CoroutineScope
        get() = plugin.container.scope
}