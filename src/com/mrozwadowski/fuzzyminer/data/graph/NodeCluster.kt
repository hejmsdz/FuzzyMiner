package com.mrozwadowski.fuzzyminer.data.graph

data class NodeCluster(val nodes: Collection<PrimitiveNode>): Node {
    override val id = nodes.map { it.id }.min() ?: 0
    override var significance = nodes.sumByDouble { it.significance } / nodes.size
    override fun toString(): String {
        return "{${nodes.sortedBy { it.toString() }.joinToString(", ")}}"
    }

    override fun toCluster(): NodeCluster = this
    operator fun plus(other: NodeCluster): NodeCluster = NodeCluster(nodes + other.nodes)
}