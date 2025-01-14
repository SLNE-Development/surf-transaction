package dev.slne.transaction.standalone.currency

import dev.slne.transaction.api.currency.Currency
import dev.slne.transaction.api.currency.CurrencyScale
import dev.slne.transaction.standalone.transaction.TransactionModel
import jakarta.persistence.*
import net.kyori.adventure.text.Component
import org.hibernate.proxy.HibernateProxy

@Entity
@Table(name = "currencies")
data class CurrencyModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long? = null,

    @Column(name = "name", nullable = false)
    override val name: String,

    @Lob
    @Column(name = "display_name", nullable = false, columnDefinition = "LONGTEXT")
    override val displayName: Component,

    @Column(name = "symbol", nullable = false)
    override val symbol: String,

    @Lob
    @Column(name = "symbol_display", nullable = false, columnDefinition = "LONGTEXT")
    override val symbolDisplay: Component,

    @Column(name = "scale", nullable = false)
    override val scale: CurrencyScale,

    @Column(name = "default_currency", nullable = false)
    override val defaultCurrency: Boolean = false,

    @OneToMany(
        targetEntity = TransactionModel::class,
        fetch = FetchType.LAZY,
        mappedBy = "currency",
        orphanRemoval = true,
        cascade = [CascadeType.ALL]
    )
    private val transactions: List<TransactionModel> = listOf(),
) : Currency {

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as CurrencyModel

        return id != null && id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    override fun toString(): String {
        return "CurrencyModel(id=$id, name='$name', displayName=$displayName, symbol='$symbol', symbolDisplay=$symbolDisplay, scale=$scale, defaultCurrency=$defaultCurrency)"
    }

}