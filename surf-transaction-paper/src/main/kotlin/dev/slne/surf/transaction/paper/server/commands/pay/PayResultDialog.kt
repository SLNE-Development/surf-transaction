@file:Suppress("UnstableApiUsage")

package dev.slne.surf.transaction.paper.server.commands.pay

import dev.slne.surf.surfapi.bukkit.api.dialog.noticeDialog
import dev.slne.surf.surfapi.bukkit.api.dialog.noticeDialogWithBuilder
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import net.kyori.adventure.text.Component

fun paySuccessDialog(
    receiverName: String,
    amount: Component,
) = noticeDialogWithBuilder(
    text("Überweisung erfolgreich", Colors.SUCCESS)
) {
    info("Du hast ")
    append(amount)
    info(" an ")
    variableValue(receiverName)
    info(" überwiesen.")
}


fun payErrorDialog() = noticeDialog(
    text("Überweisung fehlgeschlagen", Colors.ERROR),
    text("Bei der Überweisung ist ein Fehler aufgetreten.", Colors.ERROR)
)

fun paySenderInsufficientFundsDialog(
    currencyName: Component
) = noticeDialogWithBuilder(
    text("Kontostand zu niedrig", Colors.WARNING),
) {
    warning("Du hast nicht genügend ")
    append(currencyName)
    warning(".")
}

fun payReceiverInsufficientFundsDialog(
    currencyName: Component,
    receiverName: String
) = noticeDialogWithBuilder(
    text("Kontostand zu niedrig", Colors.WARNING),
) {
    variableValue(receiverName)
    warning(" hat nicht genügend ")
    append(currencyName)
    warning(".")
}