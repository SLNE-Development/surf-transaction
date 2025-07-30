package dev.slne.surf.transaction.server.netty.listener

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.cloud.api.common.netty.network.protocol.respond
import dev.slne.surf.surfapi.core.api.util.toMutableObjectSet
import dev.slne.surf.transaction.core.netty.packets.ServerboundBalancePacket
import dev.slne.surf.transaction.core.netty.packets.ServerboundExecuteSingleTransactionPacket
import dev.slne.surf.transaction.core.netty.packets.ServerboundTransferTransactionPacket
import dev.slne.surf.transaction.core.netty.packets.TransactionResultResponsePacket
import dev.slne.surf.transaction.server.transaction.TransactionService
import org.springframework.stereotype.Component

@Component
class TransactionPacketLister(private val transactionService: TransactionService) {

    @SurfNettyPacketHandler
    suspend fun handleBalance(packet: ServerboundBalancePacket) {
        packet.respond(transactionService.balanceDecimal(packet.player, packet.currency))
    }

    @SurfNettyPacketHandler
    suspend fun handleExecuteSingleTransaction(packet: ServerboundExecuteSingleTransactionPacket) {
        val result = when (packet.type) {
            ServerboundExecuteSingleTransactionPacket.Type.DEPOSIT -> transactionService.deposit(
                packet.player,
                packet.amount,
                packet.currency,
                packet.ignoreMinimum,
                *packet.additionalData
            )

            ServerboundExecuteSingleTransactionPacket.Type.WITHDRAW -> transactionService.withdraw(
                packet.player,
                packet.amount,
                packet.currency,
                packet.ignoreMinimum,
                *packet.additionalData
            )
        }

        packet.respond(TransactionResultResponsePacket(result))
    }

    @SurfNettyPacketHandler
    suspend fun handleTransferTransaction(packet: ServerboundTransferTransactionPacket) {
        val result = transactionService.transfer(
            packet.sender,
            packet.amount,
            packet.currency,
            packet.receiver,
            packet.ignoreSenderMinimum,
            packet.ignoreReceiverMinimum,
            packet.additionalSenderData.toMutableObjectSet(),
            packet.additionalReceiverData.toMutableObjectSet()
        )

        packet.respond(TransactionResultResponsePacket(result))
    }
}