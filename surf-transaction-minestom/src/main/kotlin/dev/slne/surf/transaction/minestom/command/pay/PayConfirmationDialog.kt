package dev.slne.surf.transaction.minestom.command.pay

import dev.slne.surf.api.minestom.dialog.base
import dev.slne.surf.api.minestom.dialog.dialog
import dev.slne.surf.api.minestom.dialog.type
import dev.slne.surf.transaction.core.client.component.ClientComponents
import net.kyori.adventure.text.Component
import net.minestom.server.dialog.DialogAfterAction
import net.minestom.server.entity.Player
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("SuspendCoroutineLacksCancellationGuarantees")
suspend fun payConfirmationDialog(
    sender: Player,
    receiverName: String,
    amount: Component
) = suspendCoroutine { continuation ->
    sender.showDialog(dialog {
        base {
            title(ClientComponents.Pay.CONFIRMATION_TITLE)
            preventClosingWithEscape()
            afterAction(DialogAfterAction.WAIT_FOR_RESPONSE)
            body {
                plainMessage(ClientComponents.Pay.confirmationBody(receiverName, amount))
            }
        }

        type {
            confirmation {
                no {
                    label(ClientComponents.Pay.CONFIRMATION_CANCEL_LABEL)
                    action {
                        playerCallback {
                            it.showDialog(payErrorDialog())
                            it.closeDialog()
                            continuation.resume(false)
                        }
                    }
                }
                yes {
                    label(ClientComponents.Pay.CONFIRMATION_CONFIRM_LABEL)
                    action {
                        playerCallback {
                            continuation.resume(true)
                        }
                    }
                }
            }
        }
    })
}
