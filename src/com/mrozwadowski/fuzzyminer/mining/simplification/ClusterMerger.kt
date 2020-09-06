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
        val incomingEdgesMap = edges.filter { it.target == a || it.target == b }.groupBy { it.source }
        val outgoingEdgesMap = edges.filter { it.source == a || it.source == b }.groupBy { it.target }

        incomingEdgesMap.forEach { source, incomingEdges ->
            edges.removeAll(incomingEdges)
            val significance = incomingEdges.map { it.significance }.max() ?: 0.0
            val correlation = incomingEdges.map { it.correlation }.max() ?: 0.0
            edges.add(Edge(source, newCluster, significance, correlation))
        }

        outgoingEdgesMap.forEach { target, outgoingEdges ->
            edges.removeAll(outgoingEdges)
            val significance = outgoingEdges.map { it.significance }.max() ?: 0.0
            val correlation = outgoingEdges.map { it.correlation }.max() ?: 0.0
            edges.add(Edge(newCluster, target, significance, correlation))
        }
    }
}