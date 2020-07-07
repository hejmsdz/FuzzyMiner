package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster

class ClusterFilter(private val graph: Graph) {
    private val nodes = graph.nodes.toMutableList()
    private val edges = graph.allEdgeObjects().toMutableList()

    fun apply(): Graph {
        removeDisconnectedNodes()
        removeSingularClusters()

        val edgeMap = edges.groupBy { it.source }
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
        incoming.forEach { inEdge ->
            outgoing.forEach { outEdge ->
                val significance = (inEdge.significance + outEdge.significance) / 2.0
                val correlation = (inEdge.correlation + outEdge.correlation) / 2.0
                edges.add(Edge(inEdge.source, outEdge.target, significance, correlation))
            }
        }
    }
}