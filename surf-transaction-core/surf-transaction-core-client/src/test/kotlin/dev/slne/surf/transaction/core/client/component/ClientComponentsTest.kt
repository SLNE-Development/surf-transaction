package dev.slne.surf.transaction.core.client.component

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyScale
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ClientComponentsTest {

    private val infoPrefix = Colors.INFO_PREFIX.plainText()
    private val errorPrefix = Colors.ERROR_PREFIX.plainText()

    @Test
    fun `own balance names the amount and the currency`() {
        val rendered = ClientComponents.Balance.own(TestCurrency, BigDecimal("12.5")).plainText()

        assertEquals("${infoPrefix}Dein Kontostand beträgt 12.50 Taler.", rendered)
    }

    @Test
    fun `payment notification names sender, amount and currency`() {
        val rendered = ClientComponents.paymentReceived(TestCurrency, 5.0, "Notch").plainText()

        assertEquals("${infoPrefix}Du hast 5.00 Taler von Notch erhalten.", rendered)
    }

    @Test
    fun `admin notification distinguishes added from removed`() {
        assertEquals(
            "${infoPrefix}[Admin] Du hast 5.00 Taler von Notch erhalten!",
            ClientComponents.Transaction.adminNotification(TestCurrency, 5.0, "Notch", added = true)
                .plainText()
        )

        assertEquals(
            "${infoPrefix}[Admin] Notch hat dir 5.00 Taler abgezogen!",
            ClientComponents.Transaction.adminNotification(
                TestCurrency,
                5.0,
                "Notch",
                added = false
            ).plainText()
        )
    }

    @Test
    fun `admin failure is prefixed as an error`() {
        assertEquals(
            "${errorPrefix}Es ist ein Fehler aufgetreten!",
            ClientComponents.Transaction.adminFailure().plainText()
        )
    }

    @Test
    fun `pay confirmation body lists receiver and amount`() {
        val rendered = ClientComponents.Pay.confirmationBody(
            "Notch",
            TestCurrency.format(BigDecimal.ONE)
        ).plainText()

        assertEquals("Empfänger: Notch\nBetrag: 1.00 Taler", rendered)
    }

    @Test
    fun `pay success body names receiver and amount`() {
        val rendered = ClientComponents.Pay.successBody(
            "Notch",
            TestCurrency.format(BigDecimal.ONE)
        ).plainText()

        assertEquals("Du hast 1.00 Taler an Notch überwiesen.", rendered)
    }

    @Test
    fun `insufficient funds bodies name the affected side`() {
        assertEquals(
            "Du hast nicht genügend Taler.",
            ClientComponents.Pay.senderInsufficientFundsBody(TestCurrency.displayName).plainText()
        )

        assertEquals(
            "Notch hat nicht genügend Taler.",
            ClientComponents.Pay.receiverInsufficientFundsBody(TestCurrency.displayName, "Notch")
                .plainText()
        )
    }

    @Test
    fun `already default currency names the currency`() {
        assertEquals(
            "Currency 'taler' is already the default currency.",
            ClientComponents.CurrencyMessages.alreadyDefault(TestCurrency)
        )
    }

    @Test
    fun `already existing currency renders its display name`() {
        assertEquals(
            "Die Währung Taler existiert bereits!",
            ClientComponents.CurrencyMessages.alreadyExists(TestCurrency).plainText()
        )
    }

    private object TestCurrency : Currency {
        override val name = "taler"
        override val displayName: Component = Component.text("Taler")
        override val defaultCurrency = false
        override val symbol = "T"
        override val symbolDisplay: Component = Component.text("Taler")
        override val scale = CurrencyScale.DECIMAL_2
        override val minimumAmount: BigDecimal = BigDecimal.ONE

        override fun format(amount: BigDecimal, color: TextColor): Component = Component.text()
            .append(Component.text(scale.format(amount).toPlainString()))
            .append(Component.space())
            .append(symbolDisplay)
            .build()
    }
}

private fun Component.plainText(): String = buildString { appendPlainText(this@plainText) }

private fun StringBuilder.appendPlainText(component: Component) {
    if (component is TextComponent) {
        append(component.content())
    }

    component.children().forEach { appendPlainText(it) }
}
