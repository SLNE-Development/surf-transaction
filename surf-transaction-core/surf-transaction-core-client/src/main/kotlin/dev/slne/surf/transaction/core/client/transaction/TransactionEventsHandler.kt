package dev.slne.surf.transaction.core.client.transaction

import dev.slne.surf.redis.event.OnRedisEvent
import dev.slne.surf.transaction.core.client.component.ClientComponents
import dev.slne.surf.transaction.core.client.notification.playTransactionNotificationSound
import dev.slne.surf.transaction.core.client.platform.transactionPlatform
import dev.slne.surf.transaction.paper.redis.events.transaction.AdminTransactionEvent

/**
 * Notifies a player whose balance an administrator changed on another server.
 */
class TransactionEventsHandler {

    @OnRedisEvent
    fun onAdminTransactionAddEvent(event: AdminTransactionEvent) {
        transactionPlatform.withOnlinePlayer(event.receiverUuid) { receiver ->
            receiver.sendMessage(
                ClientComponents.Transaction.adminNotification(
                    event.currency,
                    event.amount,
                    event.senderName,
                    event.added
                )
            )

            receiver.playTransactionNotificationSound()
        }
    }
}
