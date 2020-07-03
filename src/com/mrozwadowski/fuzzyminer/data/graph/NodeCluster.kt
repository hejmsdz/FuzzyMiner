package com.mrozwadowski.fuzzyminer.data.graph

data class NodeCluster(val nodes: Collection<Node>): Node {
    override val id = nodes.map { it.id }.min() ?: 0
    override var significance = 0.0
    override fun toString(): String {
        return "{${nodes.joinToString(", ")}}"
    }

    override fun toCluster(): NodeCluster = this
    operator fun plus(other: NodeCluster): NodeCluster = NodeCluster(nodes + other.nodes)
}