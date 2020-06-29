package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.EdgeMetric
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.abs

typealias NodePairs = Collection<Pair<Node, Node>>

class ConcurrencyFilter(
    private val graph: Graph,
    private val binSignificance: EdgeMetric
) {
    private val logger = Logger.getLogger(javaClass.name)

    fun apply(preserveThreshold: Double, ratioThreshold: Double): Graph {
        val conflicts = conflictedPairs()
        val edgesToRemove = conflicts.flatMap { (a, b) ->
            val ab = relativeSignificance(a, b)
            val ba = relativeSignificance(b, a)
            val offset = abs(ab - ba)

            logger.log(Level.FINER, "Rel. significance: $ab, $ba")
            if (ab >= preserveThreshold && ba >= preserveThreshold) {
                handleLength2Loop(a, b)
            } else if (offset >= ratioThreshold) {
                if (ab > ba) {
                    handleException(b, a)
                } else {
                    handleException(a, b)
                }
            } else {
                handleConcurrency(a, b)
            }
        }

        return graph.withoutEdges(edgesToRemove)
    }

    private fun handleLength2Loop(a: Node, b: Node): NodePairs {
        logger.log(Level.FINE, "'$a' and '$b' form a loop")
        return listOf()
    }

    private fun handleException(a: Node, b: Node): NodePairs {
        logger.log(Level.FINE, "'$a' following '$b' is an exception")
        return listOf(a to b)
    }

    private fun handleConcurrency(a: Node, b: Node): NodePairs {
        logger.log(Level.FINE, "'$a' and '$b' are concurrent")
        return listOf(a to b, b to a)
    }

    private fun conflictedPairs(): NodePairs {
        return findConflicts(graph).map { set ->
            assert(set.size == 2)
            val (first, second) = set.toList()
            first to second
        }
    }

    private fun relativeSignificance(a: Node, b: Node): Double {
        val abSignificance = binSignificance.calculate(a, b) * 0.5
        val axSignificance = graph.nodes.sumByDouble { binSignificance.calculate(a, it) }
        val xbSignificance = graph.nodes.sumByDouble { binSignificance.calculate(it, b) }
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