package dev.slne.surf.transaction.microservice.rabbit

import dev.slne.surf.rabbitmq.annotation.RabbitHandler
import dev.slne.surf.transaction.api.account.AccountService
import dev.slne.surf.transaction.api.currency.CurrencyService
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.core.account.CoreAccountService
import dev.slne.surf.transaction.core.currency.CoreCurrencyService
import dev.slne.surf.transaction.core.rabbit.account.AddMemberToAccountRequest
import dev.slne.surf.transaction.core.rabbit.account.AddMemberToAccountResponse
import dev.slne.surf.transaction.core.rabbit.account.CompleteAccountNameSuggestionsRequest
import dev.slne.surf.transaction.core.rabbit.account.CompleteAccountNameSuggestionsResponse
import dev.slne.surf.transaction.core.rabbit.account.CreateAccountRequest
import dev.slne.surf.transaction.core.rabbit.account.CreateAccountResponse
import dev.slne.surf.transaction.core.rabbit.account.DeleteAccountRequest
import dev.slne.surf.transaction.core.rabbit.account.DeleteAccountResponse
import dev.slne.surf.transaction.core.rabbit.account.GetAccountByAccountIdRequest
import dev.slne.surf.transaction.core.rabbit.account.GetAccountByAccountIdResponse
import dev.slne.surf.transaction.core.rabbit.account.GetAccountByNameRequest
import dev.slne.surf.transaction.core.rabbit.account.GetAccountByNameResponse
import dev.slne.surf.transaction.core.rabbit.account.GetAllAccountsByOwnerRequest
import dev.slne.surf.transaction.core.rabbit.account.GetAllAccountsByOwnerResponse
import dev.slne.surf.transaction.core.rabbit.account.GetDefaultAccountRequest
import dev.slne.surf.transaction.core.rabbit.account.GetDefaultAccountResponse
import dev.slne.surf.transaction.core.rabbit.account.RemoveMemberFromAccountRequest
import dev.slne.surf.transaction.core.rabbit.account.RemoveMemberFromAccountResponse
import dev.slne.surf.transaction.core.rabbit.currency.CreateCurrencyRequest
import dev.slne.surf.transaction.core.rabbit.currency.CreateCurrencyResponse
import dev.slne.surf.transaction.core.rabbit.currency.GetAllCurrenciesRequest
import dev.slne.surf.transaction.core.rabbit.currency.GetAllCurrenciesResponse
import dev.slne.surf.transaction.core.rabbit.currency.MakeDefaultCurrencyRequest
import dev.slne.surf.transaction.core.rabbit.currency.MakeDefaultCurrencyResponse
import dev.slne.surf.transaction.core.rabbit.transaction.BalanceRequest
import dev.slne.surf.transaction.core.rabbit.transaction.BalanceResponse
import dev.slne.surf.transaction.core.rabbit.transaction.DepositRequest
import dev.slne.surf.transaction.core.rabbit.transaction.DepositResponse
import dev.slne.surf.transaction.core.rabbit.transaction.TransactionResultPacket
import dev.slne.surf.transaction.core.rabbit.transaction.TransferRequest
import dev.slne.surf.transaction.core.rabbit.transaction.TransferResponse
import dev.slne.surf.transaction.core.rabbit.transaction.WithdrawRequest
import dev.slne.surf.transaction.core.rabbit.transaction.WithdrawResponse
import dev.slne.surf.transaction.core.transaction.CoreTransactionService
import dev.slne.surf.transaction.core.account.AccountImpl as CoreAccountImpl

class TransactionRabbitHandler {

    // ─── Account handlers ────────────────────────────────────────────────────

    @RabbitHandler
    fun handleGetAccountByAccountId(request: GetAccountByAccountIdRequest) {
        request.launch {
            val account = CoreAccountService.get().getAccountByAccountId(request.accountId)
            request.respond(GetAccountByAccountIdResponse(account as? CoreAccountImpl))
        }
    }

    @RabbitHandler
    fun handleGetAccountByName(request: GetAccountByNameRequest) {
        request.launch {
            val account = CoreAccountService.get().getAccountByName(request.name)
            request.respond(GetAccountByNameResponse(account as? CoreAccountImpl))
        }
    }

    @RabbitHandler
    fun handleCreateAccount(request: CreateAccountRequest) {
        request.launch {
            val result = CoreAccountService.get().createAccount(request.ownerUuid, request.name)
            val (account, failureReason) = when (result) {
                is dev.slne.surf.transaction.api.account.result.AccountCreationResult.Success ->
                    (result.account as? CoreAccountImpl) to null
                is dev.slne.surf.transaction.api.account.result.AccountCreationResult.Failed ->
                    null to result.reason
            }
            request.respond(CreateAccountResponse(account, failureReason))
        }
    }

    @RabbitHandler
    fun handleGetDefaultAccount(request: GetDefaultAccountRequest) {
        request.launch {
            val account = CoreAccountService.get().getDefaultAccount(request.playerUuid)
            request.respond(GetDefaultAccountResponse(account as CoreAccountImpl))
        }
    }

    @RabbitHandler
    fun handleGetAllAccountsByOwner(request: GetAllAccountsByOwnerRequest) {
        request.launch {
            val accounts = CoreAccountService.get().getAllAccountsByOwner(request.ownerUuid)
            request.respond(GetAllAccountsByOwnerResponse(accounts.filterIsInstance<CoreAccountImpl>()))
        }
    }

    @RabbitHandler
    fun handleDeleteAccount(request: DeleteAccountRequest) {
        request.launch {
            val account = AccountService.instance.getAccountByAccountId(request.accountId)
            val result = if (account != null) {
                CoreAccountService.get().deleteAccount(account)
            } else {
                dev.slne.surf.transaction.api.account.result.AccountDeleteResult.ACCOUNT_NOT_FOUND
            }
            request.respond(DeleteAccountResponse(result))
        }
    }

    @RabbitHandler
    fun handleAddMemberToAccount(request: AddMemberToAccountRequest) {
        request.launch {
            val result = CoreAccountService.get().addMemberToAccount(
                request.accountId,
                request.executor,
                request.target
            )
            request.respond(AddMemberToAccountResponse(result))
        }
    }

    @RabbitHandler
    fun handleRemoveMemberFromAccount(request: RemoveMemberFromAccountRequest) {
        request.launch {
            val result = CoreAccountService.get().removeMemberFromAccount(
                request.accountId,
                request.executor,
                request.target
            )
            request.respond(RemoveMemberFromAccountResponse(result))
        }
    }

    @RabbitHandler
    fun handleCompleteAccountNameSuggestions(request: CompleteAccountNameSuggestionsRequest) {
        request.launch {
            val suggestions = CoreAccountService.get().completeAccountNameSuggestions(
                request.input,
                request.maxSuggestions
            )
            request.respond(CompleteAccountNameSuggestionsResponse(suggestions))
        }
    }

    // ─── Currency handlers ───────────────────────────────────────────────────

    @RabbitHandler
    fun handleGetAllCurrencies(request: GetAllCurrenciesRequest) {
        request.launch {
            val currencies =
                CoreCurrencyService.get().currencies.filterIsInstance<dev.slne.surf.transaction.core.currency.CurrencyImpl>()
            val defaultCurrencyName = CoreCurrencyService.get().defaultCurrency.name
            request.respond(GetAllCurrenciesResponse(currencies, defaultCurrencyName))
        }
    }

    @RabbitHandler
    fun handleCreateCurrency(request: CreateCurrencyRequest) {
        request.launch {
            val result = CoreCurrencyService.get().createCurrency(request.currency)
            request.respond(CreateCurrencyResponse(result))
        }
    }

    @RabbitHandler
    fun handleMakeDefaultCurrency(request: MakeDefaultCurrencyRequest) {
        request.launch {
            val currency = CurrencyService.instance.getCurrencyByName(request.currencyName)
            val result = if (currency != null) {
                CoreCurrencyService.get().makeDefaultCurrency(
                    currency as dev.slne.surf.transaction.core.currency.CurrencyImpl
                )
            } else {
                dev.slne.surf.transaction.core.currency.CurrencyDefaultResult.NOT_FOUND
            }
            request.respond(MakeDefaultCurrencyResponse(result))
        }
    }

    // ─── Transaction handlers ────────────────────────────────────────────────

    @RabbitHandler
    fun handleDeposit(request: DepositRequest) {
        request.launch {
            val account = AccountService.instance.getAccountByAccountId(request.accountId)
                ?: error("Account not found: ${request.accountId}")
            val currency = CurrencyService.instance.getCurrencyByName(request.currencyName)
                ?: error("Currency not found: ${request.currencyName}")

            val result = CoreTransactionService.get().deposit(
                account,
                request.initiator,
                request.amount,
                currency,
                request.ignoreMinimum,
                *request.additionalData.toTypedArray()
            )
            request.respond(DepositResponse(result.toPacket()))
        }
    }

    @RabbitHandler
    fun handleWithdraw(request: WithdrawRequest) {
        request.launch {
            val account = AccountService.instance.getAccountByAccountId(request.accountId)
                ?: error("Account not found: ${request.accountId}")
            val currency = CurrencyService.instance.getCurrencyByName(request.currencyName)
                ?: error("Currency not found: ${request.currencyName}")

            val result = CoreTransactionService.get().withdraw(
                account,
                request.initiator,
                request.amount,
                currency,
                request.ignoreMinimum,
                *request.additionalData.toTypedArray()
            )
            request.respond(WithdrawResponse(result.toPacket()))
        }
    }

    @RabbitHandler
    fun handleTransfer(request: TransferRequest) {
        request.launch {
            val sender = AccountService.instance.getAccountByAccountId(request.senderAccountId)
                ?: error("Sender account not found: ${request.senderAccountId}")
            val receiver = AccountService.instance.getAccountByAccountId(request.receiverAccountId)
                ?: error("Receiver account not found: ${request.receiverAccountId}")
            val currency = CurrencyService.instance.getCurrencyByName(request.currencyName)
                ?: error("Currency not found: ${request.currencyName}")

            val result = CoreTransactionService.get().transfer(
                request.initiator,
                sender,
                request.amount,
                currency,
                receiver,
                request.ignoreSenderMinimum,
                request.ignoreReceiverMinimum,
                request.additionalSenderData,
                request.additionalReceiverData
            )
            request.respond(TransferResponse(result.toPacket()))
        }
    }

    @RabbitHandler
    fun handleBalance(request: BalanceRequest) {
        request.launch {
            val account = AccountService.instance.getAccountByAccountId(request.accountId)
                ?: error("Account not found: ${request.accountId}")
            val currency = CurrencyService.instance.getCurrencyByName(request.currencyName)
                ?: error("Currency not found: ${request.currencyName}")

            val balance = CoreTransactionService.get().balance(account, currency)
            request.respond(BalanceResponse(balance))
        }
    }

    private fun TransactionResult.toPacket(): TransactionResultPacket = when (this) {
        is TransactionResult.Success ->
            TransactionResultPacket.Success(transaction as dev.slne.surf.transaction.core.transaction.TransactionImpl)
        is TransactionResult.TransferSuccess ->
            TransactionResultPacket.TransferSuccess(
                senderTransaction as dev.slne.surf.transaction.core.transaction.TransactionImpl,
                receiverTransaction as dev.slne.surf.transaction.core.transaction.TransactionImpl
            )
        TransactionResult.ReceiverInsufficientFunds ->
            TransactionResultPacket.ReceiverInsufficientFunds
        TransactionResult.SenderInsufficientFunds ->
            TransactionResultPacket.SenderInsufficientFunds
        is TransactionResult.DatabaseError ->
            TransactionResultPacket.DatabaseError(cause.message ?: "Unknown error")
    }
}
