package dev.slne.surf.transaction.paper.commands

import dev.slne.surf.api.paper.permission.PermissionRegistry
import dev.slne.surf.transaction.core.client.command.TransactionPermissions


object CommandPermission : PermissionRegistry() {
    val BALANCE = create(TransactionPermissions.BALANCE)
    val BALANCE_OTHER = create(TransactionPermissions.BALANCE_OTHER)

    val CURRENCY = create(TransactionPermissions.CURRENCY)
    val CURRENCY_ADMIN = create(TransactionPermissions.CURRENCY_ADMIN)
    val CURRENCY_ADMIN_CREATE = create(TransactionPermissions.CURRENCY_ADMIN_CREATE)
    val CURRENCY_ADMIN_MAKE_DEFAULT = create(TransactionPermissions.CURRENCY_ADMIN_MAKE_DEFAULT)
    val CURRENCY_ADMIN_LIST = create(TransactionPermissions.CURRENCY_ADMIN_LIST)

    val PAY = create(TransactionPermissions.PAY)

    val TRANSACTION = create(TransactionPermissions.TRANSACTION)
    val TRANSACTION_ADMIN = create(TransactionPermissions.TRANSACTION_ADMIN)
    val TRANSACTION_ADMIN_ADD = create(TransactionPermissions.TRANSACTION_ADMIN_ADD)
    val TRANSACTION_ADMIN_REMOVE = create(TransactionPermissions.TRANSACTION_ADMIN_REMOVE)

    val ACCOUNT = create(TransactionPermissions.ACCOUNT)
    val ACCOUNT_INFO = create(TransactionPermissions.ACCOUNT_INFO)
    val ACCOUNT_LIST = create(TransactionPermissions.ACCOUNT_LIST)
    val ACCOUNT_CREATE = create(TransactionPermissions.ACCOUNT_CREATE)
    val ACCOUNT_DELETE = create(TransactionPermissions.ACCOUNT_DELETE)
    val ACCOUNT_MEMBER = create(TransactionPermissions.ACCOUNT_MEMBER)
    val ACCOUNT_MEMBER_ADD = create(TransactionPermissions.ACCOUNT_MEMBER_ADD)
    val ACCOUNT_MEMBER_REMOVE = create(TransactionPermissions.ACCOUNT_MEMBER_REMOVE)
    val ACCOUNT_MEMBER_LIST = create(TransactionPermissions.ACCOUNT_MEMBER_LIST)

}
