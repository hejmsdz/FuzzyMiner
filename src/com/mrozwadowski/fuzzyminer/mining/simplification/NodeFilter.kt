package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.EdgeMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.NodeMetric

class NodeFilter(
    private val graph: Graph,
    private val significance: NodeMetric,
    private val binCorrelation: EdgeMetric
) {
    fun apply(cutoff: Double) {
        val victims = findVictims(cutoff)
        val clusters = clustersFromAssignment(assignInitialClusters(victims))

//        clusters.forEach { cluster ->
//            val predecessors = cluster.flatMap { graph.edgesFrom(it).map { edge -> edge.source } }
//            val successors = cluster.flatMap { graph.edgesTo(it).map { edge -> edge.target } }
//            val predecessorsAreClusters = victims.containsAll(predecessors)
//            val successorsAreClusters = victims.containsAll(successors)
//        }
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