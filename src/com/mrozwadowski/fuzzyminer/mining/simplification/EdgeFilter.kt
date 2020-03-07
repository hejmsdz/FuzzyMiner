package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryCorrelationMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.BinarySignificanceMetric

class EdgeFilter<EventClass>(
    private val graph: Graph<EventClass>,
    private val binSignificance: BinarySignificanceMetric<EventClass>,
    private val binCorrelation: BinaryCorrelationMetric<EventClass>
) {
    fun filterEdges(utilityRatio: Double, cutoff: Double): Graph<EventClass> {
        val allEdges = graph.allEdges()
        val edgesToRemove = graph.nodes
            .map { it to graph.edgesFrom(it) }
            .filterNot { (_, edges) -> edges.isEmpty() }
            .flatMap { (source, edges) ->
                relativeUtilities(source, edges, utilityRatio)
                    .filterValues { it < cutoff }
                    .keys
                    .map { source to it.target }
        }

        val preservedEdges = (allEdges - edgesToRemove)
            .groupBy { it.first }
            .mapValues { it.value.map { (_, target) -> Edge(target) } }

        return Graph(graph.nodes, preservedEdges)
    }

    fun relativeUtilities(source: Node<EventClass>, edges: Collection<Edge<EventClass>>, utilityRatio: Double): Map<Edge<EventClass>, Double> {
        val utilities = edges.associateWith {
            utility(source.eventClass, it.target.eventClass, utilityRatio)
        }
        val min = utilities.values.min()!!
        val max = utilities.values.max()!!
        val range = max - min
        return utilities.mapValues { (it.value - min) / range }
    }

    private fun utility(a: EventClass, b: EventClass, utilityRatio: Double): Double {
        val significance = binSignificance.calculate(a, b).toDouble()
        val correlation = binCorrelation.calculate(a, b).toDouble()
        return utilityRatio * significance + (1 - utilityRatio) * correlation
    }
}