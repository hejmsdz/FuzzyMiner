package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster

class ClusterFilter(
    private val graph: Graph
) {
    private val nodes = graph.nodes.toMutableList()
    private val edges = graph.allEdges()

    public fun apply(): Graph {
        removeDisconnectedNodes()
//        removeSingularClusters()

        val edgeMap = edges.groupBy { it.first }
            .mapValues { it.value.map { (source, target) -> Edge(source, target) } }
        return Graph(nodes, edgeMap)
    }

    private fun removeDisconnectedNodes() {
        nodes.removeIf { graph.edgesFrom(it).isEmpty() && graph.edgesTo(it).isEmpty() }
    }

    private fun removeSingularClusters() {
        val singularClusters = nodes.filterIsInstance<NodeCluster>().filter { it.nodes.size == 1 }
    }
}