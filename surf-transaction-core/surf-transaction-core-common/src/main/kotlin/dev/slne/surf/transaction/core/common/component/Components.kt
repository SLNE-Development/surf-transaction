package dev.slne.surf.transaction.core.common.component

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.text
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.core.service.PlayerLookupService
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.account.result.AccountCreationResult.FailureReason.*
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import dev.slne.surf.transaction.core.common.account.AccountImpl
import dev.slne.surf.transaction.core.common.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.common.currency.CurrencyDefaultResult
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import java.util.*
import dev.slne.surf.transaction.api.account.Account as ApiAccount

object Components {
    object Account {
        suspend fun displayName(account: AccountImpl) = buildText {
            variableValue(account.name)
            hoverEvent(buildText {
                append {
                    variableKey("Account Id: ")
                    variableValue(account.accountId.toString())
                }
                appendNewline()
                appendNewlineAsync {
                    variableKey("Besitzer: ")
                    variableValue(usernameOrUuid(account.ownerUuid))
                }
                appendNewline()
                appendNewline {
                    variableKey("Standardkonto: ")
                    yesOrNo(account.defaultAccount)
                }
            })
        }

        suspend fun formatCreationResult(result: AccountCreationResult) = buildText {
            when (result) {
                is AccountCreationResult.Success -> {
                    success("Das Konto ")
                    append(displayName(result.account as AccountImpl))
                    success(" wurde erfolgreich erstellt")
                }

                is AccountCreationResult.Failed -> {
                    when (result.reason) {
                        NAME_ALREADY_EXISTS -> error("Ein Konto mit diesem Namen existiert bereits")
                        NAME_TOO_LONG -> error("Der Name darf maximal ${ApiAccount.MAX_NAME_LENGTH} Zeichen lang sein")
                        NAME_TOO_SHORT -> error("Der Name muss mindestens ${ApiAccount.MIN_NAME_LENGTH} Zeichen lang sein")
                        NAME_IS_UUID -> error("Der Name darf keine UUID sein")
                    }
                }
            }
        }

        fun formatDeletionResult(result: AccountDeleteResult) = buildText {
            when (result) {
                AccountDeleteResult.SUCCESS -> success("Das Konto wurde erfolgreich gelöscht")
                AccountDeleteResult.DEFAULT_ACCOUNT_CANNOT_BE_DELETED -> error("Das Standardkonto kann nicht gelöscht werden")
                AccountDeleteResult.ACCOUNT_NOT_FOUND -> error("Das Konto konnte nicht gefunden werden")
            }
        }

        object Member {
            suspend fun formatResult(result: AccountMemberResult, member: UUID, added: Boolean) =
                buildText {
                    when (result) {
                        AccountMemberResult.SUCCESS -> {
                            variableValue(usernameOrUuid(member))
                            success(" wurde erfolgreich als Mitglied ")
                            if (added) {
                                success("hinzugefügt")
                            } else {
                                success("entfernt")
                            }
                        }

                        AccountMemberResult.NOTHING_CHANGED -> {
                            if (added) {
                                variableValue(usernameOrUuid(member))
                                error(" ist bereits ein Mitglied des Kontos")
                            } else {
                                variableValue(usernameOrUuid(member))
                                error(" ist kein Mitglied des Kontos")
                            }
                        }

                        AccountMemberResult.ACCOUNT_NOT_FOUND -> error("Das Konto konnte nicht gefunden werden")
                    }
                }
        }
    }

    object Currency {
        fun formatCreateResult(currency: CurrencyImpl, result: CurrencyCreateResult) = buildText {
            when (result) {
                CurrencyCreateResult.SUCCESS -> {
                    success("Die Währung ")
                    append(currency)
                    success(" wurde erfolgreich erstellt")
                }

                CurrencyCreateResult.ALREADY_EXISTS -> {
                    error("Die Währung ")
                    append(currency)
                    error(" existiert bereits!")
                }

                CurrencyCreateResult.DEFAULT_ALREADY_EXISTS -> {
                    error("Es existiert bereits eine Standardwährung!")
                }

                CurrencyCreateResult.INVALID_NAME -> {
                    error("Der Name '")
                    variableValue(currency.name)
                    error("' ist ungültig!")
                }

                CurrencyCreateResult.INVALID_SYMBOL -> {
                    error("Das Symbol '")
                    variableValue(currency.symbol)
                    error("' ist ungültig!")
                }
            }
        }

        fun formatChangedDefaultResult(
            currency: CurrencyImpl,
            result: CurrencyDefaultResult
        ) = buildText {
            when (result) {
                CurrencyDefaultResult.SUCCESS -> {
                    append(currency)
                    success(" ist nun die Standardwährung")
                }

                CurrencyDefaultResult.NOT_FOUND -> {
                    error("Die Währung ")
                    append(currency)
                    error(" wurde nicht gefunden")
                }
            }
        }
    }

    private fun SurfComponentBuilder.yesOrNo(value: Boolean) {
        if (value) {
            success("Ja")
        } else {
            error("Nein")
        }
    }

    suspend fun usernameOrUuid(uuid: UUID): String {
        val name = PlayerLookupService.getUsername(uuid)
        return name ?: uuid.toString()
    }

    suspend fun usernameOrUuidComponent(uuid: UUID) =
        text(usernameOrUuid(uuid), Colors.VARIABLE_VALUE)
}