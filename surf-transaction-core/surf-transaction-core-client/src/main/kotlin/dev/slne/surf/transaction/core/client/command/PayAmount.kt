package dev.slne.surf.transaction.core.client.command

/**
 * The amount a single transfer is allowed to move.
 */
object PayAmount {
    const val MINIMUM = 1.0
    const val MAXIMUM = 9_999_999.0
}
