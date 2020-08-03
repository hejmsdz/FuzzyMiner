package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.utils.significantlyLess

class EdgeFilter(private val graph: Graph) {
    fun apply(utilityRatio: Double, cutoff: Double): Graph {
        val edgesToRemove = graph.nodes
            .map { graph.edgesFrom(it) }
            .filterNot { it.isEmpty() }
            .flatMap { edges ->
                relativeUtilities(edges, utilityRatio)
                    .filterValues { significantlyLess(it, cutoff) }
                    .keys
                    .map { it.source to it.target }
        }

        return graph.withoutEdges(edgesToRemove)
    }

    fun relativeUtilities(edges: Collection<Edge>, utilityRatio: Double): Map<Edge, Double> {
        val utilities = edges.associateWith {
            utility(it.source, it.target, utilityRatio)
        }
        val min = utilities.values.min()!!
        val max = utilities.values.max()!!
        val range = max - min
        return utilities.mapValues { (it.value - min) / range }
    }

    private fun utility(a: Node, b: Node, utilityRatio: Double): Double {
        val edge = graph.edgeBetween(a, b) ?: return 0.0
        return utilityRatio * edge.significance + (1 - utilityRatio) * edge.correlation
    }
}