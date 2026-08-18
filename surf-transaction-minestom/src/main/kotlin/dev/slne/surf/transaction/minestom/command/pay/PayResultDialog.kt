package dev.slne.surf.transaction.minestom.command.pay

import dev.slne.surf.api.minestom.dialog.noticeDialog
import dev.slne.surf.transaction.core.client.component.ClientComponents
import net.kyori.adventure.text.Component

fun paySuccessDialog(
    receiverName: String,
    amount: Component,
) = noticeDialog(
    ClientComponents.Pay.SUCCESS_TITLE,
    ClientComponents.Pay.successBody(receiverName, amount)
)


fun payErrorDialog() = noticeDialog(
    ClientComponents.Pay.FAILURE_TITLE,
    ClientComponents.Pay.FAILURE_BODY
)

fun paySenderInsufficientFundsDialog(
    currencyName: Component
) = noticeDialog(
    ClientComponents.Pay.INSUFFICIENT_FUNDS_TITLE,
    ClientComponents.Pay.senderInsufficientFundsBody(currencyName)
)

fun payReceiverInsufficientFundsDialog(
    currencyName: Component,
    receiverName: String
) = noticeDialog(
    ClientComponents.Pay.INSUFFICIENT_FUNDS_TITLE,
    ClientComponents.Pay.receiverInsufficientFundsBody(currencyName, receiverName)
)
