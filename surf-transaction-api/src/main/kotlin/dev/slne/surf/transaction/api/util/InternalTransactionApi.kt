package dev.slne.surf.transaction.api.util

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is intended for internal use within the Surf Transaction module. " +
            "It may change or be removed in future versions without notice. " +
            "Use with caution."
)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY,
         AnnotationTarget.TYPEALIAS, AnnotationTarget.VALUE_PARAMETER)
annotation class InternalTransactionApi
