package dev.slne.surf.transaction.api.util

/**
 * Marks declarations that are strictly internal to the Surf Transaction module.
 *
 * APIs annotated with [InternalTransactionApi] are not part of the public API
 * contract and must not be used by external consumers.
 *
 * These APIs may change, break, or be removed at any time without notice.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is strictly internal to the Surf Transaction module and MUST NOT be used. " +
            "It is not part of the public API and may change, break, or be removed at any time " +
            "without notice."
)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.VALUE_PARAMETER
)
annotation class InternalTransactionApi
