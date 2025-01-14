package dev.slne.transaction.standalone.transaction

import dev.slne.transaction.api.currency.Currency
import dev.slne.transaction.api.transaction.Transaction
import dev.slne.transaction.api.user.TransactionUser
import dev.slne.transaction.api.user.transactionUserManager
import dev.slne.transaction.standalone.currency.CurrencyModel
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.proxy.HibernateProxy
import org.hibernate.type.SqlTypes
import org.jetbrains.annotations.Unmodifiable
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.*

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "transactions")
data class TransactionModel(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long? = null,

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "identifier", nullable = false, length = 36, unique = true)
    override val identifier: UUID,

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "sender", nullable = true, length = 36)
    private val senderUuid: UUID?,

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "receiver", nullable = true, length = 36)
    private val receiverUuid: UUID?,

    @Column(name = "amount", nullable = false, precision = 65, scale = 2)
    override val amount: BigDecimal,

    @ManyToOne(targetEntity = CurrencyModel::class, fetch = FetchType.LAZY, cascade = [CascadeType.ALL], optional = false)
    @JoinColumn(name = "currency_id", nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    override val currency: Currency,

    @Lob
    @Column(name = "extra", nullable = true)
    @Convert(converter = TransactionExtraConverter::class)
    private val _extra: Map<String, String>,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    override val createdAt: ZonedDateTime? = null,

    @LastModifiedDate
    @Column(name = "updated_at", nullable = true, updatable = true, insertable = false)
    override val updatedAt: ZonedDateTime? = null,
) : Transaction {

    override val sender: TransactionUser?
        get() = senderUuid?.let { transactionUserManager[it] }

    override val receiver: TransactionUser?
        get() = receiverUuid?.let { transactionUserManager[it] }

    override val extra: @Unmodifiable Object2ObjectMap<String, String>
        get() = Object2ObjectMaps.unmodifiable(Object2ObjectOpenHashMap(_extra))

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass =
            if (other is HibernateProxy) other.hibernateLazyInitializer.persistentClass else other.javaClass
        val thisEffectiveClass =
            if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass else this.javaClass
        if (thisEffectiveClass != oEffectiveClass) return false
        other as TransactionModel

        return id != null && id == other.id
    }

    final override fun hashCode(): Int =
        if (this is HibernateProxy) this.hibernateLazyInitializer.persistentClass.hashCode() else javaClass.hashCode()

    override fun toString(): String {
        return "TransactionModel(createdAt=$createdAt, id=$id, identifier=$identifier, sender=$sender, receiver=$receiver, amount=$amount, currency=$currency, _extra=$_extra, updatedAt=$updatedAt, extra=$extra)"
    }

}
