package dev.slne.surf.transaction.paper

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.TransactionInstance
import dev.slne.surf.transaction.paper.commands.CommandManager
import java.nio.file.Path

@AutoService(TransactionInstance::class)
class PaperTransactionInstance: TransactionInstance() {
    override val dataPath: Path
        get() = plugin.dataPath

    override suspend fun load() {
        super.load()
        CommandManager().registerCommands()
    }
}