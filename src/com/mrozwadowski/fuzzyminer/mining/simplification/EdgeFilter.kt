package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.EdgeMetric

class EdgeFilter(
    private val graph: Graph,
    private val binSignificance: EdgeMetric,
    private val binCorrelation: EdgeMetric
) {
    fun apply(utilityRatio: Double, cutoff: Double): Graph {
        val edgesToRemove = graph.nodes
            .map { graph.edgesFrom(it) }
            .filterNot { it.isEmpty() }
            .flatMap { edges ->
                relativeUtilities(edges, utilityRatio)
                    .filterValues { it < cutoff }
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
        val significance = binSignificance.calculate(a, b)
        val correlation = binCorrelation.calculate(a, b)
        return utilityRatio * significance + (1 - utilityRatio) * correlation
    }
}