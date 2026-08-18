package dev.slne.surf.transaction.core.client.notification

import dev.slne.surf.api.core.generated.SoundKeys
import dev.slne.surf.api.core.messages.adventure.playSound
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.sound.Sound

/**
 * Plays the sound that accompanies a balance change notification.
 */
fun Audience.playTransactionNotificationSound() {
    playSound(true) {
        type(SoundKeys.ENTITY_CHICKEN_EGG)
        volume(0.5f)
        source(Sound.Source.PLAYER)
    }
}
