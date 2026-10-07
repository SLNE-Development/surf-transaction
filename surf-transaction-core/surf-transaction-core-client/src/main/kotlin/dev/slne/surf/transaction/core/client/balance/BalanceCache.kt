package dev.slne.surf.transaction.core.client.balance

import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.core.client.account.AccountServiceImpl
import dev.slne.surf.transaction.core.client.rabbitApi
import dev.slne.surf.transaction.core.client.redis.RedisService
import dev.slne.surf.transaction.core.client.redis.events.balance.BalanceChangedEvent
import dev.slne.surf.transaction.core.common.protocol.transaction.balance.GetTransactionBalanceRequestPacket
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

/**
 * Locally cached balances of default accounts, readable without suspending.
 *
 * Cached balances are never recalculated locally: whenever a transaction touches an account,
 * the balance is queried from the microservice again (see [balancesChanged]).
 */
object BalanceCache {
    private val log = logger()
    private val expireAfterAccess = 10.minutes.toJavaDuration()

    private val scope =
        CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("surf-transaction-balance-cache") + CoroutineExceptionHandler { context, throwable ->
            log.atSevere()
                .withCause(throwable)
                .log("Unhandled exception in ${context[CoroutineName]}")
        })

    private val defaultAccountIds: AsyncLoadingCache<UUID, UUID> = Caffeine.newBuilder()
        .expireAfterAccess(expireAfterAccess)
        .buildAsync { playerUuid, _ ->
            scope.future { AccountServiceImpl.INSTANCE.getDefaultAccount(playerUuid).accountId }
        }

    private val balances: Cache<BalanceKey, CachedBalance> = Caffeine.newBuilder()
        .expireAfterAccess(expireAfterAccess)
        .build()

    /**
     * Returns the cached balance of the default account of [playerUuid] in [currency].
     *
     * Returns `null` while the balance is not cached yet and starts loading it in the background.
     */
    fun cachedBalance(playerUuid: UUID, currency: Currency): BigDecimal? {
        val accountId = defaultAccountIds.get(playerUuid).getNowOrNull() ?: return null
        val key = BalanceKey(accountId, currency.name)

        return balances.get(key) { CachedBalance().also { refresh(key, it) } }.value
    }

    /**
     * Re-queries the cached balances touched by [transactions] on this server and notifies all
     * other servers to do the same.
     */
    fun balancesChanged(transactions: Collection<Transaction>) {
        if (transactions.isEmpty()) return

        transactions.groupBy { it.currency.name }.forEach { (currencyName, changed) ->
            val accountIds = changed
                .flatMap { listOfNotNull(it.senderAccountId, it.receiverAccountId) }
                .toSet()

            refreshAccounts(accountIds, currencyName)

            scope.launch {
                RedisService.publish(BalanceChangedEvent(accountIds, currencyName)).await()
            }
        }
    }

    /**
     * Re-queries the balances of [accountIds] in [currencyName] that are cached on this server.
     */
    fun refreshAccounts(accountIds: Collection<UUID>, currencyName: String) {
        for (accountId in accountIds) {
            val key = BalanceKey(accountId, currencyName)
            val entry = balances.policy().getIfPresentQuietly(key) ?: continue

            refresh(key, entry)
        }
    }

    fun disposeScope() {
        scope.cancel("Disposing BalanceCache scope")
    }

    private fun refresh(key: BalanceKey, entry: CachedBalance) {
        val version = entry.nextVersion()

        scope.launch {
            try {
                val request = GetTransactionBalanceRequestPacket(key.accountId, key.currencyName)
                val (balance) = rabbitApi.sendRequest(request)

                entry.update(version, balance)
            } catch (cause: CancellationException) {
                throw cause
            } catch (cause: Throwable) {
                log.atWarning()
                    .withCause(cause)
                    .log("Failed to refresh balance of account ${key.accountId} in ${key.currencyName}")

                // Never loaded: drop the entry so the next access retries instead of staying empty.
                if (entry.value == null) {
                    balances.asMap().remove(key, entry)
                }
            }
        }
    }

    private fun <T : Any> CompletableFuture<T>.getNowOrNull(): T? =
        if (isDone && !isCompletedExceptionally) join() else null

    private data class BalanceKey(val accountId: UUID, val currencyName: String)

    private class CachedBalance {
        @Volatile
        var value: BigDecimal? = null
            private set

        private var requestedVersion = 0L
        private var appliedVersion = 0L

        @Synchronized
        fun nextVersion() = ++requestedVersion

        /** Applies [balance] unless a refresh that started later has already been applied. */
        @Synchronized
        fun update(version: Long, balance: BigDecimal) {
            if (version < appliedVersion) return

            appliedVersion = version
            value = balance
        }
    }
}
