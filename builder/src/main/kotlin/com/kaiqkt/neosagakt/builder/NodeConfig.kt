package com.kaiqkt.neosagakt.builder

import com.kaiqkt.neosagakt.core.Command
import com.kaiqkt.neosagakt.core.Node

public fun NodeConfig.toNode(nextOnSuccess: Node? = null, nextOnFailure: Node? = null): Node =
    Node(
        command = command,
        nextOnSuccess = nextOnSuccess,
        nextOnFailure = nextOnFailure,
        maxNumberOfAttempts = maxNumberOfAttempts
    )

public data class NodeConfig(val command: Class<out Command>, val maxNumberOfAttempts: Int = Int.MAX_VALUE)

public inline fun <reified T : Command> NodeConfig(maxNumberOfAttempts: Int = Int.MAX_VALUE): NodeConfig =
    NodeConfig(T::class.java, maxNumberOfAttempts)