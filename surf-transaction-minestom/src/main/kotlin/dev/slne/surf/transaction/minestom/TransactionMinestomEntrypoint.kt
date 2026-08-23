package dev.slne.surf.transaction.minestom

import com.google.inject.Inject
import com.google.inject.Singleton
import dev.slne.minestom.lobby.api.plugin.MinestomPluginEntrypoint
import dev.slne.minestom.lobby.api.plugin.annotation.DataDirectory
import dev.slne.surf.transaction.core.common.TransactionInstance
import java.nio.file.Path

@Singleton
class TransactionMinestomEntrypoint @Inject constructor(
    @DataDirectory path: Path
) : MinestomPluginEntrypoint {

    init {
        dataPath = path
    }

    override suspend fun start() {
        TransactionInstance.INSTANCE.load()
        TransactionInstance.INSTANCE.enable()
    }

    override suspend fun stop() {
        TransactionInstance.INSTANCE.disable()
    }

    companion object {
        @Volatile
        lateinit var dataPath: Path
    }
}
