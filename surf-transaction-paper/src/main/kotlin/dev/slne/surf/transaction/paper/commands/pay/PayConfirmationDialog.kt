@file:Suppress("UnstableApiUsage")

package dev.slne.surf.transaction.paper.commands.pay

import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
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
            title {
                primary("Überweisung überprüfen")
            }
            preventClosingWithEscape()
            afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)
            body {
                plainMessage {
                    append {
                        variableKey("Empfänger: ")
                        variableValue(receiverName)
                    }
                    appendNewline {
                        variableKey("Betrag: ")
                        append(amount)
                    }
                }
            }
        }

        type {
            confirmation {
                no {
                    label { error("Abbrechen") }
                    action {
                        playerCallback {
                            it.showDialog(payErrorDialog())
                            it.closeDialog()
                            continuation.resume(false)
                        }
                    }
                }
                yes {
                    label { success("Bestätigen") }
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