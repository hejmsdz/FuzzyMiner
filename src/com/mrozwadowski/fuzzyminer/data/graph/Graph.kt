package com.mrozwadowski.fuzzyminer.data.graph

data class Graph(
    val nodes: List<Node>,
    private val edges: Map<Node, List<Edge>>
) {
    fun edgesFrom(source: Node): Collection<Edge> {
        return edges.getOrDefault(source, listOf())
    }

    fun edgesTo(node: Node): Collection<Edge> {
        return edges.values.flatten().filter { it.target == node }
    }

    fun neighbors(node: Node): Collection<Node> {
        return edgesFrom(node).map { it.target } + edgesTo(node).map { it.source }
    }

    fun edgeBetween(source: Node, target: Node): Edge? {
        return edgesFrom(source).find { it.target == target }
    }

    fun allEdges(): Collection<Pair<Node, Node>> {
        return allEdgeObjects().map { it.source to it.target }
    }

    fun allEdgeObjects(): Collection<Edge> {
        return edges.values.flatten()
    }

    fun withoutEdges(edgesToRemove: Collection<Pair<Node, Node>>): Graph {
        val preservedEdges = allEdgeObjects()
            .filter { !edgesToRemove.contains(it.source to it.target) }
            .groupBy { it.source }

        return Graph(nodes, preservedEdges)
    }
}