package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster

class ClusterFilter(
    private val graph: Graph
) {
    private val nodes = graph.nodes.toMutableList()
    private val edges = graph.allEdges().toMutableList()

    public fun apply(): Graph {
        removeDisconnectedNodes()
        removeSingularClusters()

        val edgeMap = edges.groupBy { it.first }
            .mapValues { it.value.map { (source, target) -> Edge(source, target) } }
        return Graph(nodes, edgeMap)
    }

    private fun removeDisconnectedNodes() {
        nodes.removeIf { graph.edgesFrom(it).isEmpty() && graph.edgesTo(it).isEmpty() }
    }

    private fun removeSingularClusters() {
        val singularClusters = nodes.filter { it is NodeCluster && it.nodes.size == 1 }
        singularClusters.forEach { connectTransitive(it) }
        nodes.removeAll(singularClusters)
    }

    private fun connectTransitive(node: Node) {
        val incoming = edges.filter { (_, target) -> target == node }
        val outgoing = edges.filter { (source, _) -> source == node }
        edges.removeAll(incoming)
        edges.removeAll(outgoing)
        incoming.forEach { (source, _) ->
            outgoing.forEach { (_, target) ->
                edges.add(source to target)
            }
        }
    }
}