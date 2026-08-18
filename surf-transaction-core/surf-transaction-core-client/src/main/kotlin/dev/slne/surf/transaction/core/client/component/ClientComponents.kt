package dev.slne.surf.transaction.core.client.component

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.core.messages.pagination.Pagination
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.common.component.Components
import net.kyori.adventure.text.Component
import java.math.BigDecimal
import java.util.UUID

/**
 * The messages the transaction commands and notifications send to their audience.
 */
object ClientComponents {

    object Balance {
        /**
         * The balance message for a player asking about their own balance.
         */
        fun own(currency: Currency, balance: BigDecimal) = buildText {
            appendInfoPrefix()
            info("Dein Kontostand beträgt ")
            append(currency.format(balance))
            info(".")
        }

        /**
         * The balance message for an audience asking about the balance of [queryTarget].
         */
        suspend fun other(
            currency: Currency,
            balance: BigDecimal,
            queryTarget: UUID
        ) = buildText {
            appendInfoPrefix()
            info("Der Kontostand von ")
            append(Components.usernameOrUuidComponent(queryTarget))
            info(" beträgt ")
            append(currency.format(balance))
            info(".")
        }
    }

    object Pay {
        const val SELF_TRANSFER_NOT_ALLOWED = "Du kannst dir kein Geld selbst überweisen!"

        val CONFIRMATION_TITLE: Component = buildText { primary("Überweisung überprüfen") }
        val CONFIRMATION_CANCEL_LABEL: Component = buildText { error("Abbrechen") }
        val CONFIRMATION_CONFIRM_LABEL: Component = buildText { success("Bestätigen") }

        val SUCCESS_TITLE: Component = text("Überweisung erfolgreich", Colors.SUCCESS)
        val FAILURE_TITLE: Component = text("Überweisung fehlgeschlagen", Colors.ERROR)
        val FAILURE_BODY: Component =
            text("Bei der Überweisung ist ein Fehler aufgetreten.", Colors.ERROR)
        val INSUFFICIENT_FUNDS_TITLE: Component = text("Kontostand zu niedrig", Colors.WARNING)

        /**
         * The dialog body listing the receiver and the amount about to be transferred.
         */
        fun confirmationBody(receiverName: String, amount: Component) = buildText {
            append {
                variableKey("Empfänger: ")
                variableValue(receiverName)
            }
            appendNewline {
                variableKey("Betrag: ")
                append(amount)
            }
        }

        /**
         * The dialog body confirming a completed transfer.
         */
        fun successBody(receiverName: String, amount: Component) = buildText {
            info("Du hast ")
            append(amount)
            info(" an ")
            variableValue(receiverName)
            info(" überwiesen.")
        }

        /**
         * The dialog body telling the sender that their own balance is too low.
         */
        fun senderInsufficientFundsBody(currencyName: Component) = buildText {
            warning("Du hast nicht genügend ")
            append(currencyName)
            warning(".")
        }

        /**
         * The dialog body telling the sender that the receiver's balance is too low.
         */
        fun receiverInsufficientFundsBody(
            currencyName: Component,
            receiverName: String
        ) = buildText {
            variableValue(receiverName)
            warning(" hat nicht genügend ")
            append(currencyName)
            warning(".")
        }
    }

    object Transaction {
        /**
         * The message shown to an administrator whose transaction failed.
         */
        fun adminFailure() = buildText {
            appendErrorPrefix()
            error("Es ist ein Fehler aufgetreten!")
        }

        /**
         * The message shown to an administrator who added money to [receiverUuid].
         */
        suspend fun adminAdded(
            currency: Currency,
            amount: Double,
            receiverUuid: UUID
        ) = buildText {
            appendSuccessPrefix()
            appendAdminTag()

            success("Du hast ")
            append(currency.format(amount))
            success(" an ")
            variableValue(Components.usernameOrUuid(receiverUuid))
            success(" gesendet!")
        }

        /**
         * The message shown to an administrator who removed money from [receiverUuid].
         */
        suspend fun adminRemoved(
            currency: Currency,
            amount: Double,
            receiverUuid: UUID
        ) = buildText {
            appendSuccessPrefix()
            appendAdminTag()

            success("Du hast ")
            append(currency.format(amount.toBigDecimal()))
            success(" von ")
            variableValue(Components.usernameOrUuid(receiverUuid))
            success(" abgezogen!")
        }

        /**
         * The notification for a player whose balance an administrator changed.
         */
        fun adminNotification(
            currency: Currency,
            amount: Double,
            senderName: String,
            added: Boolean
        ) = buildText {
            appendInfoPrefix()
            appendAdminTag()

            if (added) {
                info("Du hast ")
                append(currency.format(amount))
                info(" von ")
                variableValue(senderName)
                info(" erhalten!")
            } else {
                variableValue(senderName)
                info(" hat dir ")
                append(currency.format(amount))
                info(" abgezogen!")
            }
        }
    }

    object CurrencyMessages {
        /**
         * The error shown when a currency with the same name already exists.
         */
        fun alreadyExists(currency: Currency) = buildText {
            error("Die Währung ")
            append(currency)
            error(" existiert bereits!")
        }

        /**
         * The error shown when the currency is already the default currency.
         */
        fun alreadyDefault(currency: Currency) =
            "Currency '${currency.name}' is already the default currency."

        /**
         * The paginated listing of all known currencies.
         */
        val pagination by lazy {
            Pagination<Currency> {
                title { primary("Currencies") }
                rowRenderer { currency, _ ->
                    listOf(
                        buildText {
                            append(CommonComponents.EM_DASH)
                            appendSpace()
                            append(currency)
                            appendSpace()
                            append(CommonComponents.EM_DASH)
                            variableKey(" Default: ")
                            variableValue(currency.defaultCurrency)
                        }
                    )
                }
            }
        }
    }

    /**
     * The notification for a player who received money from another player.
     */
    fun paymentReceived(
        currency: Currency,
        amount: Double,
        senderName: String
    ) = buildText {
        appendInfoPrefix()

        info("Du hast ")
        append(currency.format(amount))
        info(" von ")
        variableValue(senderName)
        info(" erhalten.")
    }
}

private fun SurfComponentBuilder.appendAdminTag() {
    darkSpacer("[")
    variableKey("Admin")
    darkSpacer("] ")
}
