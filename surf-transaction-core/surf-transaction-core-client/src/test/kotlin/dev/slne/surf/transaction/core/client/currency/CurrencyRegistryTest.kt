package dev.slne.surf.transaction.core.client.currency

import dev.slne.surf.transaction.api.currency.CurrencyScale
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import net.kyori.adventure.text.Component
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

class CurrencyRegistryTest {

    @Test
    fun `name lookup is case insensitive and keeps the first match`() {
        val first = currency("taler")
        val registry = CurrencyRegistry(listOf(first, currency("crown")), first)

        assertSame(first, registry.byName("TALER"))
        assertSame(first, registry.byName("taler"))
        assertNotNull(registry.byName("Crown"))
        assertNull(registry.byName("guilder"))
    }

    @Test
    fun `adding a known currency keeps the registry unchanged`() {
        val base = currency("taler", default = true)
        val registry = CurrencyRegistry(listOf(base), base)

        assertSame(registry, registry.plus(base))
    }

    @Test
    fun `changing the default moves the flag to exactly one currency`() {
        val old = currency("taler", default = true)
        val new = currency("crown")
        val updated = CurrencyRegistry(listOf(old, new), old).withDefault("CROWN")

        assertNotNull(updated)
        checkNotNull(updated)
        assertEquals("crown", updated.defaultCurrency.name)
        assertEquals(1, updated.currencies.count { it.defaultCurrency })
        assertFalse(checkNotNull(updated.byName("taler")).defaultCurrency)
    }

    @Test
    fun `changing the default to an unknown currency reports no change`() {
        val base = currency("taler", default = true)

        assertNull(CurrencyRegistry(listOf(base), base).withDefault("guilder"))
    }

    @Test
    fun `concurrent additions never lose a currency`() {
        val base = currency("base", default = true)
        val registry = AtomicReference(CurrencyRegistry(listOf(base), base))

        runConcurrently(threadCount = 8, iterations = 16) { thread, iteration ->
            val added = currency("currency-$thread-$iteration")
            registry.updateAndGet { current -> current.plus(added) }
        }

        val result = registry.get()
        assertEquals(8 * 16 + 1, result.currencies.size)
        for (thread in 0 until 8) {
            for (iteration in 0 until 16) {
                assertNotNull(
                    result.byName("currency-$thread-$iteration"),
                    "lost currency-$thread-$iteration"
                )
            }
        }
        assertSame(base, result.defaultCurrency)
    }

    @Test
    fun `concurrent default changes leave exactly one default currency`() {
        val all = List(8) { index -> currency("currency-$index", default = index == 0) }
        val registry = AtomicReference(CurrencyRegistry(all, all.first()))

        runConcurrently(threadCount = 8, iterations = 200) { thread, _ ->
            registry.updateAndGet { current -> current.withDefault("currency-$thread") ?: current }
        }

        val result = registry.get()
        assertEquals(all.size, result.currencies.size)
        assertEquals(1, result.currencies.count { it.defaultCurrency })
        assertTrue(result.defaultCurrency.defaultCurrency)
        assertSame(result.defaultCurrency, result.byName(result.defaultCurrency.name))
    }

    private fun currency(name: String, default: Boolean = false) = CurrencyImpl(
        name = name,
        displayName = Component.text(name),
        symbol = name.take(2),
        symbolDisplay = Component.text(name.take(2)),
        scale = CurrencyScale.INTEGER,
        minimumAmount = BigDecimal.ZERO,
        defaultCurrency = default
    )

    private fun runConcurrently(
        threadCount: Int,
        iterations: Int,
        action: (thread: Int, iteration: Int) -> Unit
    ) {
        val start = CountDownLatch(1)
        val failures = CopyOnWriteArrayList<Throwable>()

        val threads = List(threadCount) { thread ->
            Thread {
                start.await()
                repeat(iterations) { iteration -> action(thread, iteration) }
            }.apply {
                isDaemon = true
                setUncaughtExceptionHandler { _, cause -> failures += cause }
                start()
            }
        }

        start.countDown()
        threads.forEach { it.join(30_000) }
        threads.forEach { assertFalse(it.isAlive, "worker thread did not terminate") }
        assertTrue(failures.isEmpty(), "worker threads failed: $failures")
    }
}
