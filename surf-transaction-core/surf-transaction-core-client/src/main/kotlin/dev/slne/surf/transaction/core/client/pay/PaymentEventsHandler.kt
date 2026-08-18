package dev.slne.surf.transaction.core.client.pay

import dev.slne.surf.redis.event.OnRedisEvent
import dev.slne.surf.transaction.core.client.component.ClientComponents
import dev.slne.surf.transaction.core.client.notification.playTransactionNotificationSound
import dev.slne.surf.transaction.core.client.platform.transactionPlatform
import dev.slne.surf.transaction.paper.redis.events.pay.PaymentReceivedEvent

/**
 * Notifies the receiver of a payment that was made on another server.
 */
class PaymentEventsHandler {

    @OnRedisEvent
    fun onPaymentReceived(event: PaymentReceivedEvent) {
        transactionPlatform.withOnlinePlayer(event.receiverUUID) { receiver ->
            receiver.sendMessage(
                ClientComponents.paymentReceived(
                    event.currency,
                    event.amount,
                    event.senderName
                )
            )

            receiver.playTransactionNotificationSound()
        }
    }
}
