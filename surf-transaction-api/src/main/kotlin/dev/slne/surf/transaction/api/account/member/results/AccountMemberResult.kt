package dev.slne.surf.transaction.api.account.member.results

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.SerializableUUID
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.util.ComponentResult
import kotlinx.serialization.Serializable

@Serializable
sealed class AccountMemberResult : ComponentResult {
    @Serializable
    data class AddSuccess(
        val accountId: SerializableUUID,
        val executorUuid: SerializableUUID,
        val targetUuid: SerializableUUID,
    ) : AccountMemberResult() {
        override val isSuccess = true

        override suspend fun SurfComponentBuilder.buildMessage() {
            val account = Account[accountId]
            val target = OfflineCloudPlayer[targetUuid]

            val accountName = account?.name ?: accountId.toString()

            append(target.displayName())
            success(" wurde erfolgreich zum Konto ")
            variableValue(accountName)
            success(" hinzugefügt.")
        }
    }

    @Serializable
    data class AlreadyMember(
        val accountId: SerializableUUID,
        val executorUuid: SerializableUUID,
        val targetUuid: SerializableUUID,
    ) : AccountMemberResult() {
        override val isSuccess = false

        override suspend fun SurfComponentBuilder.buildMessage() {
            val account = Account[accountId]
            val target = OfflineCloudPlayer[targetUuid]

            val accountName = account?.name ?: accountId.toString()

            append(target.displayName())
            error(" ist bereits Mitglied des Kontos ")
            variableValue(accountName)
            error(".")
        }
    }

    @Serializable
    data class RemoveSuccess(
        val accountId: SerializableUUID,
        val executorUuid: SerializableUUID,
        val targetUuid: SerializableUUID,
    ) : AccountMemberResult() {
        override val isSuccess = true

        override suspend fun SurfComponentBuilder.buildMessage() {
            val account = Account[accountId]
            val target = OfflineCloudPlayer[targetUuid]

            val accountName = account?.name ?: accountId.toString()

            append(target.displayName())
            success(" wurde erfolgreich vom Konto ")
            variableValue(accountName)
            success(" entfernt.")
        }
    }

    @Serializable
    data class NotMember(
        val accountId: SerializableUUID,
        val executorUuid: SerializableUUID,
        val targetUuid: SerializableUUID,
    ) : AccountMemberResult() {
        override val isSuccess = false

        override suspend fun SurfComponentBuilder.buildMessage() {
            val account = Account[accountId]
            val target = OfflineCloudPlayer[targetUuid]

            val accountName = account?.name ?: accountId.toString()

            append(target.displayName())
            error(" ist kein Mitglied des Kontos ")
            variableValue(accountName)
            error(".")
        }
    }

}