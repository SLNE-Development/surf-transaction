package dev.slne.surf.transaction.paper.server

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.TransactionInstance
import java.nio.file.Path

@AutoService(TransactionInstance::class)
class PaperTransactionInstance: TransactionInstance() {
    override val dataPath: Path
        get() = plugin.dataPath

    override suspend fun load() {
        super.load()
        _root_ide_package_.dev.slne.surf.transaction.paper.server.commands.CommandManager().registerCommands()
    }
}