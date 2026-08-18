package dev.slne.surf.transaction.minestom.command

import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player

private const val CONSOLE_NAME = "CONSOLE"

/**
 * The name this sender is recorded and announced under.
 */
val CommandSender.senderName: String get() = (this as? Player)?.username ?: CONSOLE_NAME

/**
 * The stable identity this sender is recorded under: a player's uuid, or the console's name.
 */
val CommandSender.senderId: String get() = (this as? Player)?.uuid?.toString() ?: senderName
