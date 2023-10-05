package com.kaiqkt.neosagakt.core

public data class Transaction(
    val id: String,
    val node: Node,
    val context: Map<String, Any>,
    val attempt: Int = 0
) {
    public fun nextOnSuccess(): Transaction? = this.node.nextOnSuccess?.let { copy(node = it, attempt = 0) }
    public fun nextOnFailure(): Transaction? = this.node.nextOnFailure?.let { copy(node = it, attempt = 0) }
    public fun exceedMaxTryRetryAttempts(): Boolean = this.attempt >= this.node.maxNumberOfAttempts
}