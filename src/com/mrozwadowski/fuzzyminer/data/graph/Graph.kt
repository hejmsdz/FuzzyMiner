package com.mrozwadowski.fuzzyminer.data.graph

data class Graph<EventClass>(
    val nodes: List<Node<EventClass>>,
    private val edges: Map<Node<EventClass>, List<Edge<EventClass>>>
) {
    fun edgesFrom(source: Node<EventClass>): Collection<Edge<EventClass>> {
        return edges.getOrDefault(source, listOf())
    }

    fun neighbours(node: Node<EventClass>): Collection<Node<EventClass>> {
        return edgesFrom(node).map { it.target }
    }

    fun edgeBetween(source: Node<EventClass>, target: Node<EventClass>): Edge<EventClass>? {
        return edgesFrom(source).find { it.target == target }
    }

    fun allEdges(): Collection<Pair<Node<EventClass>, Node<EventClass>>> {
        return edges.flatMap { (source, edgesFromA) ->
            edgesFromA.map { source to it.target }
        }
    }
}