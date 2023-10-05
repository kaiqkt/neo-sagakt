package com.kaiqkt.neosagakt.core

public data class Node(
    val command: Class<out Command>,
    val nextOnSuccess: Node? = null,
    val nextOnFailure: Node? = null,
    val maxNumberOfAttempts: Int = Int.MAX_VALUE
)

public inline fun <reified T : Command> Node(
    nextOnSuccess: Node? = null,
    nextOnFailure: Node? = null,
    maxNumberOfAttempts: Int = Int.MAX_VALUE
): Node = Node(T::class.java, nextOnSuccess, nextOnFailure, maxNumberOfAttempts)