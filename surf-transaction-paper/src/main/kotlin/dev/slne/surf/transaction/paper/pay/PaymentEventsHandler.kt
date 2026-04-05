package dev.slne.surf.transaction.paper.pay

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.generated.SoundKeys
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.redis.event.OnRedisEvent
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.transaction.paper.plugin
import dev.slne.surf.transaction.paper.redis.events.pay.PaymentReceivedEvent
import net.kyori.adventure.sound.Sound

class PaymentEventsHandler {

    @OnRedisEvent
    fun onPaymentReceived(event: PaymentReceivedEvent) {
        val receiver = server.getPlayer(event.receiverUUID) ?: return

        plugin.launch(plugin.entityDispatcher(receiver)) {
            receiver.sendText {
                appendInfoPrefix()

                info("Du hast ")
                append(event.currency.format(event.amount))
                info(" von ")
                variableValue(event.senderName)
                info(" erhalten.")
            }

            receiver.playSound(true) {
                type(SoundKeys.ENTITY_CHICKEN_EGG)
                volume(0.5f)
                source(Sound.Source.PLAYER)
            }
        }
    }
}