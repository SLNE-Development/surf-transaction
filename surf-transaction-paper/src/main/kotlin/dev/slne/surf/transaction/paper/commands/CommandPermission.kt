package dev.slne.surf.transaction.paper.commands

import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object CommandPermission : PermissionRegistry() {
    private const val PREFIX = "surf.transaction.command"

    val BALANCE = create("$PREFIX.balance")

    val CURRENCY = create("$PREFIX.currency")
    val CURRENCY_ADMIN = create("$CURRENCY.admin")
    val CURRENCY_ADMIN_CREATE = create("$CURRENCY_ADMIN.create")
    val CURRENCY_ADMIN_MAKE_DEFAULT = create("$CURRENCY_ADMIN.make-default")
    val CURRENCY_ADMIN_LIST = create("$CURRENCY_ADMIN.list")

    val PAY = create("$PREFIX.pay")

    val TRANSACTION = create("$PREFIX.transaction")
    val TRANSACTION_ADMIN = create("$TRANSACTION.admin")
    val TRANSACTION_ADMIN_ADD = create("$TRANSACTION_ADMIN.add")
    val TRANSACTION_ADMIN_REMOVE = create("$TRANSACTION_ADMIN.remove")

    val ACCOUNT = create("$PREFIX.account")
    val ACCOUNT_INFO = create("$ACCOUNT.info")
    val ACCOUNT_LIST = create("$ACCOUNT.list")
    val ACCOUNT_CREATE = create("$ACCOUNT.create")
    val ACCOUNT_DELETE = create("$ACCOUNT.delete")
    val ACCOUNT_MEMBER = create("$ACCOUNT.member")
    val ACCOUNT_MEMBER_ADD = create("$ACCOUNT_MEMBER.add")
    val ACCOUNT_MEMBER_REMOVE = create("$ACCOUNT_MEMBER.remove")
    val ACCOUNT_MEMBER_LIST = create("$ACCOUNT_MEMBER.list")

}