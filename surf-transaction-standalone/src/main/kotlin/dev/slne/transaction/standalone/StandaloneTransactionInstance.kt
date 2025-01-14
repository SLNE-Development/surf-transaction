package dev.slne.transaction.standalone

import com.google.gson.Gson

val gson
    get() = StandaloneTransactionInstance.gson

object StandaloneTransactionInstance {
    val gson = Gson()
}