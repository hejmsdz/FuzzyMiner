package com.mrozwadowski.fuzzyminer.data.graph

data class NodeCluster(val nodes: Collection<Node>): Node {
    override val id = nodes.map { it.id }.min() ?: 0
    override fun toString(): String {
        return nodes.joinToString(", ")
    }
}