package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster

class ClusterMerger(private val graph: Graph) {
    private val primitives = graph.nodes.filter { it !is NodeCluster }
    private val clusters = graph.nodes.filterIsInstance<NodeCluster>().toMutableList()
    private val edges = graph.allEdgeObjects().toMutableSet()

    fun apply(): Graph {
        while (true) {
            val clusterPair = findMergeCandidates() ?: break
            mergeClusters(clusterPair.first, clusterPair.second)
        }
        val edgeMap = edges.groupBy { it.source }
        return Graph(primitives + clusters, edgeMap)
    }

    private fun findMergeCandidates(): Pair<NodeCluster, NodeCluster>? {
        clusters.forEach { cluster ->
            val predecessors = edges.filter { it.target == cluster }.map { it.source }
            val predCandidate = chooseConnectedCluster(cluster, predecessors)
            if (predCandidate !== null) {
                return@findMergeCandidates cluster to predCandidate
            }

            val successors = edges.filter { it.source == cluster }.map { it.target }
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
        return clusterNeighbors.maxWith(compareBy(
            { graph.edgeBetween(node, it)?.correlation ?: 0.0 },
            { it.toString() }
        ))
    }

    private fun mergeClusters(a: NodeCluster, b: NodeCluster) {
        val newCluster = a + b
        clusters.remove(a)
        clusters.remove(b)
        clusters.add(newCluster)

        edges.removeIf { (it.source == a && it.target == b) || (it.source == b && it.target == a) }
        val incomingEdges = edges.filter { it.target == a || it.target == b }
        val outgoingEdges = edges.filter { it.source == a || it.source == b }
        edges.removeAll(incomingEdges)
        edges.removeAll(outgoingEdges)
        edges.addAll(incomingEdges.map { Edge(it.source, newCluster) })
        edges.addAll(outgoingEdges.map { Edge(newCluster, it.target) })
    }
}