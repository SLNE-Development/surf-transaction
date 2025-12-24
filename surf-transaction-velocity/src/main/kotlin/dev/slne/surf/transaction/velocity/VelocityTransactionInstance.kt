package dev.slne.surf.transaction.velocity

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.TransactionInstance
import java.nio.file.Path

@AutoService(TransactionInstance::class)
class VelocityTransactionInstance : TransactionInstance() {
    override val dataPath: Path
        get() = plugin.dataPath
}