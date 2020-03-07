package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
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
    fun resolveConflicts(preserveThreshold: Double, ratioThreshold: Double): Graph<EventClass> {
        val allEdges = graph.allEdges()

        val conflicts = conflictedPairs()
        val edgesToRemove = conflicts.flatMap { (a, b) ->
            val ab = relativeSignificance(a.eventClass, b.eventClass)
            val ba = relativeSignificance(b.eventClass, a.eventClass)
            val offset = abs(ab - ba)

            println("$ab $ba $offset")

            if (ab >= preserveThreshold && ba >= preserveThreshold) {
                println("$a <> $b 2-loop")
                listOf()  // 2-loop, keep both edges
            } else if (offset >= ratioThreshold) {
                // exception, remove less significant edge
                if (ab > ba) {
                    println("$b > $a exceptional")
                    listOf(b to a)
                } else {
                    println("$a > $b exceptional")
                    listOf(a to b)
                }
            } else {
                println("$a <> $b concurrency")
                listOf(a to b, b to a) // concurrency, remove both edges
            }
        }

        val preservedEdges = (allEdges - edgesToRemove)
            .groupBy { it.first }
            .mapValues { it.value.map { (_, target) -> Edge(target) } }

        return Graph(graph.nodes, preservedEdges)
    }

    private fun conflictedPairs(): Collection<Pair<Node<EventClass>, Node<EventClass>>> {
        return findConflicts(graph).map { set ->
            assert(set.size == 2)
            val (first, second) = set.toList()
            first to second
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

fun <EventClass>findConflicts(graph: Graph<EventClass>): Set<Set<Node<EventClass>>> {
    return graph.nodes.flatMap { source ->
        graph.edgesFrom(source)
            .filter { edge -> graph.edgeBetween(edge.target, source) != null && edge.target != source }
            .map { edge -> setOf(source, edge.target) }
    }.toSet()
}