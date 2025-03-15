package dev.slne.surf.transaction.velocity.commands.pay

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.surfapi.core.api.generated.SoundKeys
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transactionApi
import dev.slne.surf.transaction.velocity.commands.arguments.PlayerUuidArgumentType
import dev.slne.surf.transaction.velocity.commands.arguments.playerUuidArgument
import dev.slne.surf.transaction.velocity.plugin
import net.kyori.adventure.sound.Sound

object PayCommand : CommandAPICommand("pay") {
    init {
        withPermission("surf.transaction.pay")
        withAliases("bezahlen", "überweisen")

        playerUuidArgument("receiver")
        doubleArgument("amount", min = 1.0)

        playerExecutor { sender, args ->
            val suppliedPlayer: PlayerUuidArgumentType = args.getUnchecked("receiver") ?: run {
                sender.sendText {
                    appendPrefix()

                    error("Der Benutzer konnte nicht gefunden werden!")
                }

                return@playerExecutor
            }
            val amount: Double by args

            if (amount <= 1) {
                sender.sendText {
                    appendPrefix()

                    error("Der Betrag muss größer als 1 sein!")
                }
            }

            plugin.container.launch {
                val playerName = suppliedPlayer.first
                val uuid = suppliedPlayer.second.await() ?: run {
                    sender.sendText {
                        appendPrefix()

                        error("Der Benutzer ")
                        variableValue(playerName)
                        error(" konnte nicht gefunden werden!")
                    }

                    return@launch
                }

                val senderUser = transactionApi.getTransactionUser(sender.uniqueId)
                val receiverUser = transactionApi.getTransactionUser(uuid)
                val currency = transactionApi.getDefaultCurrency() ?: run {
                    sender.sendText {
                        appendPrefix()

                        error("Es wurde keine Standardwährung festgelegt!")
                    }

                    return@launch
                }

                val result = senderUser.transfer(amount, currency, receiverUser)
                val transactionResult = result.first

                when (transactionResult) {
                    TransactionResult.SUCCESS -> handleSuccess(
                        sender,
                        playerName,
                        amount,
                        currency
                    )

                    TransactionResult.RECEIVER_INSUFFICIENT_FUNDS -> handleReceiverInsufficientFunds(
                        sender,
                        playerName,
                        currency
                    )

                    TransactionResult.SENDER_INSUFFICIENT_FUNDS -> handleSenderInsufficientFunds(
                        sender,
                        currency
                    )

                    TransactionResult.DATABASE_ERROR -> handleError(sender)
                }
            }
        }
    }

    private fun handleSuccess(
        sender: Player,
        receiver: String,
        amount: Double,
        currency: Currency
    ) {
        sender.sendText {
            appendPrefix()

            info("Du hast ")
            append(currency.format(amount))
            info(" an ")
            variableValue(receiver)
            info(" überwiesen.")
        }

        val receiverPlayer = plugin.proxy.getPlayer(receiver).orElse(null) ?: return

        receiverPlayer.sendText {
            appendPrefix()

            info("Du hast ")
            append(currency.format(amount))
            info(" von ")
            variableValue(sender.username)
            info(" erhalten.")
        }

        receiverPlayer.playSound {
            type(SoundKeys.ENTITY_CHICKEN_EGG)
            volume(.5f)
            source(Sound.Source.PLAYER)
        }
    }

    private fun handleError(sender: Player) {
        sender.sendText {
            appendPrefix()

            error("Es ist ein Fehler aufgetreten!")
        }
    }

    private fun handleSenderInsufficientFunds(sender: Player, currency: Currency) {
        sender.sendText {
            appendPrefix()

            error("Du hast nicht genügend ")
            append(currency.displayName)
            error(" um diese Transaktion durchzuführen!")
        }
    }

    private fun handleReceiverInsufficientFunds(
        sender: Player,
        receiver: String,
        currency: Currency
    ) {
        sender.sendText {
            appendPrefix()

            error("Der Benutzer ")
            variableValue(receiver)
            error(" hat nicht genügend ")
            append(currency.displayName)
            error(" um diese Transaktion durchzuführen!")
        }
    }
}