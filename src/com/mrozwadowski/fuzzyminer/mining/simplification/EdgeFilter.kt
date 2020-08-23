package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.utils.significantlyGreater

class EdgeFilter(private val graph: Graph) {
    fun apply(utilityRatio: Double, cutoff: Double): Graph {
        val allEdges = graph.allEdges()

        val edgesToPreserve = graph.nodes
            .flatMap { listOf(graph.edgesFrom(it), graph.edgesTo(it)) }
            .filterNot { it.isEmpty() }
            .flatMap { edges ->
                relativeUtilities(edges, utilityRatio)
                    .filterValues { significantlyGreater(it, cutoff) }
                    .keys
                    .map { it.source to it.target }
        }.toSet()
        val edgesToRemove = allEdges - edgesToPreserve

        return graph.withoutEdges(edgesToRemove)
    }

    fun relativeUtilities(edges: Collection<Edge>, utilityRatio: Double): Map<Edge, Double> {
        val utilities = edges.associateWith { utility(it, utilityRatio) }
        val min = utilities.values.min()!!
        val max = utilities.values.max()!!
        val range = max - min
        return if (significantlyGreater(range, 0.0)) {
            utilities.mapValues { (it.value - min) / range }
        } else {
            utilities.mapValues { 1.0 }
        }
    }

    private fun utility(edge: Edge, utilityRatio: Double): Double {
        return utilityRatio * edge.significance + (1 - utilityRatio) * edge.correlation
    }
}