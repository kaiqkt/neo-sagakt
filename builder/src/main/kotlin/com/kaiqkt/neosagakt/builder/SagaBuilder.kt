package com.kaiqkt.neosagakt.builder

import com.kaiqkt.neosagakt.core.Node

public fun saga(
    actionsAndCompensations: List<Pair<NodeConfig, NodeConfig?>>,
    incompensableActions: List<NodeConfig> = emptyList()
): Node {
    require(actionsAndCompensations.isNotEmpty() || incompensableActions.isNotEmpty()) { "requi re least one action to start" }

    val (actions, compensations) = actionsAndCompensations.unzip()

    val incompensableGraph = buildIncompensableGraph(incompensableActions)
    val compensationsGraph = buildCompensationNodes(compensations)

    return buildActionGraph(actions, incompensableGraph, compensationsGraph)!!
}

public fun buildIncompensableGraph(
    compensations: List<NodeConfig>
): Node? = compensations.foldRight(null) { action, nextNode: Node? ->
    action.toNode(nextOnSuccess = nextNode)
}

public fun buildCompensationNodes(
    compensations: List<NodeConfig?>
): List<Node?> = compensations.fold(listOf(null)) { nodes: List<Node?>, action ->
    action?.toNode(
        nextOnSuccess = nodes.asSequence().filterNotNull().lastOrNull()
    ).let(nodes::plus)
}

public fun buildActionGraph(
    actions: List<NodeConfig>,
    incompensableNode: Node?,
    compensationsNode: List<Node?>
): Node? = actions.foldRightIndexed(incompensableNode) { index, action, firstNode: Node? ->
    action.toNode(
        nextOnSuccess = firstNode,
        nextOnFailure = compensationsNode.asSequence().take(index + 1).filterNotNull().lastOrNull()
    )
}