package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryCorrelationMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.BinarySignificanceMetric
import org.deckfour.xes.classification.XEventClass

class EdgeFilter(
    private val graph: Graph,
    private val binSignificance: BinarySignificanceMetric,
    private val binCorrelation: BinaryCorrelationMetric
) {
    fun filterEdges(utilityRatio: Double, cutoff: Double): Graph {
        val edgesToRemove = graph.nodes
            .map { it to graph.edgesFrom(it) }
            .filterNot { (_, edges) -> edges.isEmpty() }
            .flatMap { (source, edges) ->
                relativeUtilities(source, edges, utilityRatio)
                    .filterValues { it < cutoff }
                    .keys
                    .map { source to it.target }
        }

        return graph.withoutEdges(edgesToRemove)
    }

    fun relativeUtilities(source: Node, edges: Collection<Edge>, utilityRatio: Double): Map<Edge, Double> {
        val utilities = edges.associateWith {
            utility(source.eventClass, it.target.eventClass, utilityRatio)
        }
        val min = utilities.values.min()!!
        val max = utilities.values.max()!!
        val range = max - min
        return utilities.mapValues { (it.value - min) / range }
    }

    private fun utility(a: XEventClass, b: XEventClass, utilityRatio: Double): Double {
        val significance = binSignificance.calculate(a, b)
        val correlation = binCorrelation.calculate(a, b)
        return utilityRatio * significance + (1 - utilityRatio) * correlation
    }
}