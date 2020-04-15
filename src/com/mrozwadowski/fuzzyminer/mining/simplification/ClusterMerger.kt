package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.EdgeMetric

class ClusterMerger(
    private val graph: Graph,
    private val binCorrelation: EdgeMetric
) {
    private val primitives = graph.nodes.filter { it !is NodeCluster }
    private val clusters = graph.nodes.filterIsInstance<NodeCluster>().toMutableList()
    private val edges = graph.allEdges().toMutableSet()

    fun apply(): Graph {
        while (true) {
            val clusterPair = findMergeCandidates() ?: break
            mergeClusters(clusterPair.first, clusterPair.second)
        }
        val edgeMap = edges.groupBy { it.first }
            .mapValues { it.value.map { (source, target) -> Edge(source, target) } }
        return Graph(primitives + clusters, edgeMap)
    }

    private fun findMergeCandidates(): Pair<NodeCluster, NodeCluster>? {
        clusters.forEach { cluster ->
            val predecessors = graph.edgesTo(cluster).map { it.source }
            val predCandidate = chooseConnectedCluster(cluster, predecessors)
            if (predCandidate !== null) {
                return@findMergeCandidates cluster to predCandidate
            }

            val successors = graph.edgesFrom(cluster).map { it.target }
            val succCandidate = chooseConnectedCluster(cluster, successors)
            if (succCandidate !== null) {
                return@findMergeCandidates cluster to succCandidate
            }
        }
        return null
    }

    private fun chooseConnectedCluster(node: Node, neighbors: Collection<Node>): NodeCluster? {
        if (!neighbors.all { it is NodeCluster }) {
            return null
        }
        val clusterNeighbors = neighbors.filterIsInstance<NodeCluster>()
        return clusterNeighbors.maxBy { binCorrelation.calculate(node, it) }
    }

    private fun mergeClusters(a: NodeCluster, b: NodeCluster) {
        val newCluster = a + b
        clusters.removeAll(listOf(a, b))
        clusters.add(newCluster)

        edges.remove(a to b)
        val incomingEdges = edges.filter { it.second == a || it.second == b }
        val outgoingEdges = edges.filter { it.first == a || it.first == b }
        edges.removeAll(incomingEdges)
        edges.removeAll(outgoingEdges)
        edges.addAll(incomingEdges.map { it.first to newCluster })
        edges.addAll(outgoingEdges.map { newCluster to it.second })
    }
}