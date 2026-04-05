package dev.slne.surf.transaction.paper

import com.github.shynixn.mccoroutine.folia.scope
import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.client.ClientTransactionalInstance
import dev.slne.surf.transaction.core.common.TransactionInstance
import dev.slne.surf.transaction.paper.commands.CommandManager
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

@AutoService(TransactionInstance::class)
class PaperTransactionInstance: ClientTransactionalInstance() {
    override val dataPath: Path
        get() = plugin.dataPath

    override val scope: CoroutineScope
        get() = plugin.scope

    override suspend fun load() {
        super.load()
        CommandManager().registerCommands()
    }
}