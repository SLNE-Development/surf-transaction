package dev.slne.surf.transaction.paper.transaction

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.generated.SoundKeys
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.redis.event.OnRedisEvent
import dev.slne.surf.transaction.paper.plugin
import dev.slne.surf.transaction.paper.redis.events.transaction.AdminTransactionEvent
import net.kyori.adventure.sound.Sound

class TransactionEventsHandler {

    @OnRedisEvent
    fun onAdminTransactionAddEvent(event: AdminTransactionEvent) {
        val receiver = server.getPlayer(event.receiverUuid) ?: return

        plugin.launch(plugin.entityDispatcher(receiver)) {
            receiver.sendText {
                appendInfoPrefix()

                darkSpacer("[")
                variableKey("Admin")
                darkSpacer("] ")

                if (event.added) {
                    info("Du hast ")
                    append(event.currency.format(event.amount))
                    info(" von ")
                    variableValue(event.senderName)
                    info(" erhalten!")
                } else {
                    variableValue(event.senderName)
                    info(" hat dir ")
                    append(event.currency.format(event.amount))
                    info(" abgezogen!")
                }
            }

            receiver.playSound(true) {
                type(SoundKeys.ENTITY_CHICKEN_EGG)
                volume(0.5f)
                source(Sound.Source.PLAYER)
            }
        }
    }
}