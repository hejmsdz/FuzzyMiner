package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.log.Event
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.mining.metrics.BinarySignificanceMetric
import kotlin.math.abs

class ConflictResolver<EventClass>(
    private val graph: Graph<EventClass>,
    private val binSignificance: BinarySignificanceMetric<EventClass>
) {
    fun resolveConflicts(preserveThreshold: Double, ratioThreshold: Double) {
        val conflicts = conflictedPairs()
        conflicts.forEach { (a, b) ->
            val ab = relativeSignificance(a, b)
            val ba = relativeSignificance(b, a)
            val offset = abs(ab - ba)
            if (ab >= preserveThreshold && ba >= preserveThreshold) {
                println("$a -> $b 2-loop")
                // keep both edges
            } else if (offset >= ratioThreshold) {
                if (ab > ba) {
                    println("$b -> $a exception")
                } else {
                    println("$a -> $b exception")
                }
                // remove less significant edge
            } else {
                println("$a || $b concurrency")
                // remove both edges
            }
        }
    }

    private fun conflictedPairs(): Collection<Pair<EventClass, EventClass>> {
        return findConflicts(graph).map { set ->
            assert(set.size == 2)
            val (first, second) = set.toList()
            first.eventClass to second.eventClass
        }
    }

    private fun relativeSignificance(a: EventClass, b: EventClass): Double {
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
            .filter { edge -> graph.edgeBetween(edge.target, source) != null && edge.target != source }
            .map { edge -> setOf(source, edge.target) }
    }.toSet()
}