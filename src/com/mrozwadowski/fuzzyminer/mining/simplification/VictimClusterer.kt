package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.EdgeMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.NodeMetric

class VictimClusterer(
    private val graph: Graph,
    private val significance: NodeMetric,
    private val binCorrelation: EdgeMetric
) {
    fun apply(cutoff: Double): Graph {
        val victims = findVictims(cutoff).toSet()
        val clusters = clustersFromAssignment(assignInitialClusters(victims))
        val reverseClusterMap = clusters.flatMap { cluster -> cluster.nodes.map { it to cluster } }.toMap()
        val primitives = graph.nodes - victims
        val edges = graph.allEdges()
            .filter { (source, target) ->
                (source !in victims) || (target !in victims) || (reverseClusterMap[source] != reverseClusterMap[target])
            }.map { (source, target) ->
            (reverseClusterMap[source] ?: source) to (reverseClusterMap[target] ?: target)
        }
        val edgeMap = edges.groupBy { it.first }
            .mapValues { it.value.map { (source, target) -> Edge(source, target) } }
        return Graph(primitives + clusters, edgeMap)
    }

    private fun findVictims(cutoff: Double): Collection<Node> {
        return graph.nodes.filter { significance.calculate(it) < cutoff }
    }

    private fun assignInitialClusters(victims: Collection<Node>): Map<Node, Int> {
        val assignment = mutableMapOf<Node, Int>()
        var nextCluster = 1

        victims.forEach { victim ->
            val mostCorrelatedNeighbor = graph.neighbors(victim).maxBy { binCorrelation.calculate(victim, it) }
            val cluster = assignment.getOrDefault(mostCorrelatedNeighbor, 0)
            assignment[victim] = if (cluster == 0) nextCluster++ else cluster
        }

        return assignment
    }

    private fun clustersFromAssignment(assignment: Map<Node, Int>): Collection<NodeCluster> {
        return assignment.entries
            .groupBy { it.value }
            .values
            .map { entries -> NodeCluster(entries.map { it.key }) }
    }
}