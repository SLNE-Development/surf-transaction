package dev.slne.surf.transaction.core.client.command

/**
 * The permission nodes guarding this plugin's commands.
 */
object TransactionPermissions {
    private const val PREFIX = "surf.transaction.command"

    const val BALANCE = "$PREFIX.balance"
    const val BALANCE_OTHER = "$BALANCE.other"

    const val CURRENCY = "$PREFIX.currency"
    const val CURRENCY_ADMIN = "$CURRENCY.admin"
    const val CURRENCY_ADMIN_CREATE = "$CURRENCY_ADMIN.create"
    const val CURRENCY_ADMIN_MAKE_DEFAULT = "$CURRENCY_ADMIN.make-default"
    const val CURRENCY_ADMIN_LIST = "$CURRENCY_ADMIN.list"

    const val PAY = "$PREFIX.pay"

    const val TRANSACTION = "$PREFIX.transaction"
    const val TRANSACTION_ADMIN = "$TRANSACTION.admin"
    const val TRANSACTION_ADMIN_ADD = "$TRANSACTION_ADMIN.add"
    const val TRANSACTION_ADMIN_REMOVE = "$TRANSACTION_ADMIN.remove"

    const val ACCOUNT = "$PREFIX.account"
    const val ACCOUNT_INFO = "$ACCOUNT.info"
    const val ACCOUNT_LIST = "$ACCOUNT.list"
    const val ACCOUNT_CREATE = "$ACCOUNT.create"
    const val ACCOUNT_DELETE = "$ACCOUNT.delete"
    const val ACCOUNT_MEMBER = "$ACCOUNT.member"
    const val ACCOUNT_MEMBER_ADD = "$ACCOUNT_MEMBER.add"
    const val ACCOUNT_MEMBER_REMOVE = "$ACCOUNT_MEMBER.remove"
    const val ACCOUNT_MEMBER_LIST = "$ACCOUNT_MEMBER.list"
}
