package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryCorrelationMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.BinarySignificanceMetric

class EdgeFilter<EventClass>(
    private val graph: Graph<EventClass>,
    private val binSignificance: BinarySignificanceMetric<EventClass>,
    private val binCorrelation: BinaryCorrelationMetric<EventClass>
) {
    fun filterEdges(utilityRatio: Double, cutoff: Double) {
        val edgesToRemove = graph.nodes.flatMap { source ->
            val edges = graph.edgesFrom(source)
            if (edges.isEmpty()) {
                return@flatMap listOf()
            }
            val utilities = edges.associateWith {
                utility(source.eventClass, it.target.eventClass, utilityRatio)
            }
            val min = utilities.values.min()!!
            val max = utilities.values.max()!!
            val range = max - min
            utilities
                .filterValues { ((it - min) / range) < cutoff }
                .keys
                .map { source to it.target }
        }
    }

    private fun utility(a: EventClass, b: EventClass, utilityRatio: Double): Double {
        val significance = binSignificance.calculate(a, b).toDouble()
        val correlation = binCorrelation.calculate(a, b).toDouble()
        return utilityRatio * significance + (1 - utilityRatio) * correlation
    }
}