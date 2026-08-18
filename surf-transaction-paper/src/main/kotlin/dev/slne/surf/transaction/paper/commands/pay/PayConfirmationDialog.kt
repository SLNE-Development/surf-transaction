@file:Suppress("UnstableApiUsage")

package dev.slne.surf.transaction.paper.commands.pay

import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.transaction.core.client.component.ClientComponents
import io.papermc.paper.registry.data.dialog.DialogBase
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
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
            afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
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
                        callback {
                            continuation.resume(true)
                        }
                    }
                }
            }
        }
    })
}
