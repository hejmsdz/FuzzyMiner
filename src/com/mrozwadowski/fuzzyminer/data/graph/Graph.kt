package com.mrozwadowski.fuzzyminer.data.graph

data class Graph(val nodes: List<Node>, private val edges: Map<Node, List<Edge>>) {
    fun edgesFrom(source: Node): Collection<Edge> {
        return edges.getOrDefault(source, listOf())
    }

    fun neighbours(node: Node): Collection<Node> {
        return edgesFrom(node).map { it.target }
    }

    fun edgeBetween(source: Node, target: Node): Edge? {
        return edgesFrom(source).find { it.target == target }
    }

    fun allEdges(): Collection<Pair<Node, Node>> {
        return edges.flatMap { (source, edgesFromA) ->
            edgesFromA.map { source to it.target }
        }
    }
}