package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.mining.metrics.BinarySignificanceMetric
import org.deckfour.xes.classification.XEventClass
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.abs

class ConflictResolver(
    private val graph: Graph,
    private val binSignificance: BinarySignificanceMetric
) {
    private val logger = Logger.getLogger(javaClass.name)

    fun resolveConflicts(preserveThreshold: Double, ratioThreshold: Double): Graph {
        val conflicts = conflictedPairs()
        val edgesToRemove = conflicts.flatMap { (a, b) ->
            val ab = relativeSignificance(a.eventClass, b.eventClass)
            val ba = relativeSignificance(b.eventClass, a.eventClass)
            val offset = abs(ab - ba)

            logger.log(Level.FINER, "Rel. significance: $ab, $ba")
            if (ab >= preserveThreshold && ba >= preserveThreshold) {
                logger.log(Level.FINE, "'$a' and '$b' form a loop")
                listOf()  // 2-loop, keep both edges
            } else if (offset >= ratioThreshold) {
                // exception, remove less significant edge
                if (ab > ba) {
                    logger.log(Level.FINE, "'$b' following '$a' is an exception")
                    listOf(b to a)
                } else {
                    logger.log(Level.FINE, "'$a' following '$b' is an exception")
                    listOf(a to b)
                }
            } else {
                logger.log(Level.FINE, "'$a' and '$b' are concurrent")
                listOf(a to b, b to a) // concurrency, remove both edges
            }
        }

        return graph.withoutEdges(edgesToRemove)
    }

    private fun conflictedPairs(): Collection<Pair<Node, Node>> {
        return findConflicts(graph).map { set ->
            assert(set.size == 2)
            val (first, second) = set.toList()
            first to second
        }
    }

    private fun relativeSignificance(a: XEventClass, b: XEventClass): Double {
        val abSignificance = binSignificance.calculate(a, b) * 0.5
        val eventClasses = graph.nodes.map { it.eventClass }
        val axSignificance = eventClasses.sumByDouble { binSignificance.calculate(a, it) }
        val xbSignificance = eventClasses.sumByDouble { binSignificance.calculate(it, b) }
        return 0.5 * abSignificance * ((1 / axSignificance) + (1 / xbSignificance))
    }
}

fun findConflicts(graph: Graph): Set<Set<Node>> {
    return graph.nodes.flatMap { source ->
        graph.edgesFrom(source)
            .filter { edge -> graph.edgeBetween(edge.target, source) != null && edge.target != source }
            .map { edge -> setOf(source, edge.target) }
    }.toSet()
}