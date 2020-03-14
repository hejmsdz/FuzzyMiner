package com.mrozwadowski.fuzzyminer.data.graph

data class Graph(
    val nodes: List<Node>,
    private val edges: Map<Node, List<Edge>>
) {
    fun edgesFrom(source: Node): Collection<Edge> {
        return edges.getOrDefault(source, listOf())
    }

    fun edgesTo(node: Node): Collection<Edge> {
        return edges.flatMap { it.value }.filter { it.target == node }
    }

    fun neighbors(node: Node): Collection<Node> {
        return edgesFrom(node).map { it.target } + edgesTo(node).map { it.source }
    }

    fun edgeBetween(source: Node, target: Node): Edge? {
        return edgesFrom(source).find { it.target == target }
    }

    fun allEdges(): Collection<Pair<Node, Node>> {
        return edges.flatMap { (source, edgesFromA) ->
            edgesFromA.map { source to it.target }
        }
    }

    fun withoutEdges(edgesToRemove: Collection<Pair<Node, Node>>): Graph {
        val preservedEdges = (allEdges() - edgesToRemove)
            .groupBy { it.first }
            .mapValues { it.value.map { (source, target) -> Edge(source, target) } }

        return Graph(nodes, preservedEdges)
    }
}