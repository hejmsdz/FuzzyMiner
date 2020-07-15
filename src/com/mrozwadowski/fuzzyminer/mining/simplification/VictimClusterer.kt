package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.*

class VictimClusterer(private val graph: Graph) {
    fun apply(cutoff: Double): Graph {
        val victims = findVictims(cutoff).toSet()
        val clusters = clustersFromAssignment(assignInitialClusters(victims))
        val reverseClusterMap = clusters.flatMap { cluster -> cluster.nodes.map { it to cluster } }.toMap()
        val primitives = graph.nodes - victims
        val edges = graph.allEdgeObjects()
            .filter { (source, target) ->
                (source !in victims) || (target !in victims) || (reverseClusterMap[source] != reverseClusterMap[target])
            }.map { (source, target) ->
                createEdge(graph, (reverseClusterMap[source] ?: source), (reverseClusterMap[target] ?: target))
            }
        val edgeMap = edges.groupBy { it.source }
        return Graph(primitives + clusters, edgeMap)
    }

    private fun findVictims(cutoff: Double): Collection<PrimitiveNode> {
        return graph.nodes.filterIsInstance<PrimitiveNode>().filter { it.significance < cutoff }
    }

    private fun assignInitialClusters(victims: Collection<PrimitiveNode>): Map<PrimitiveNode, Int> {
        val assignment = mutableMapOf<PrimitiveNode, Int>()
        var nextCluster = 1

        victims.forEach { victim ->
            val mostCorrelatedNeighbor = graph.neighbors(victim).maxBy { graph.edgeBetween(victim, it)?.correlation ?: 0.0 }
            val cluster = assignment.getOrDefault(mostCorrelatedNeighbor, 0)
            assignment[victim] = if (cluster == 0) nextCluster++ else cluster
        }

        return assignment
    }

    private fun clustersFromAssignment(assignment: Map<PrimitiveNode, Int>): Collection<NodeCluster> {
        return assignment.entries
            .groupBy { it.value }
            .values
            .map { entries -> NodeCluster(entries.map { it.key }) }
    }
}

fun createEdge(graph: Graph, source: Node, target: Node): Edge {
    var significance = 0.0
    var correlation = 0.0

    if (source is PrimitiveNode && target is PrimitiveNode) {
        significance = graph.edgeBetween(source, target)?.significance ?: 0.0
        correlation = graph.edgeBetween(source, target)?.correlation ?: 0.0
    }
    if (source is NodeCluster && target is PrimitiveNode) {
        significance = source.nodes.map { graph.edgeBetween(it, target)?.significance ?: 0.0 }.average()
        correlation = source.nodes.map { graph.edgeBetween(it, target)?.correlation ?: 0.0 }.average()
    }
    if (source is PrimitiveNode && target is NodeCluster) {
        significance = target.nodes.map { graph.edgeBetween(source, it)?.significance ?: 0.0 }.average()
        correlation = target.nodes.map { graph.edgeBetween(source, it)?.correlation ?: 0.0 }.average()
    }
    if (source is NodeCluster && target is NodeCluster) {
        significance = source.nodes.flatMap { sourceNode ->
            target.nodes.map { targetNode ->
                graph.edgeBetween(sourceNode, targetNode)?.significance ?: 0.0
            }
        }.average()
        correlation = source.nodes.flatMap { sourceNode ->
            target.nodes.map { targetNode ->
                graph.edgeBetween(sourceNode, targetNode)?.significance ?: 0.0
            }
        }.average()
    }

    return Edge(source, target, significance, correlation)
}
