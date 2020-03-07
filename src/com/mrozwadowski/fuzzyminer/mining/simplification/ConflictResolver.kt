package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.log.Event
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.mining.metrics.BinarySignificanceMetric

class ConflictResolver<EventClass>(
    private val graph: Graph<EventClass>,
    private val binSignificance: BinarySignificanceMetric<EventClass>
) {
    fun findConflicts() = findConflicts(graph)

    fun relativeSignificance(a: EventClass, b: EventClass): Double {
        val abSignificance = binSignificance.calculate(a, b).toDouble() * 0.5
        val eventClasses = graph.nodes.map { it.eventClass }
        val axSignificance = eventClasses.sumByDouble { binSignificance.calculate(a, it).toDouble() }
        val xbSignificance = eventClasses.sumByDouble { binSignificance.calculate(it, b).toDouble() }
        return 0.5 * abSignificance * ((1 / axSignificance) + (1 / xbSignificance))
    }
}

fun <EventClass>findConflicts(graph: Graph<EventClass>): Collection<Set<Node<EventClass>>> {
    return graph.nodes.flatMap { source ->
        graph.edgesFrom(source)
            .filter { edge -> graph.edgeBetween(edge.target, source) != null }
            .map { edge -> setOf(source, edge.target) }
    }.toSet()
}