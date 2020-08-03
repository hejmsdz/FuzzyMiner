package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.utils.significantlyGreater
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.abs

typealias NodePairs = Collection<Pair<Node, Node>>

class ConcurrencyFilter(private val graph: Graph) {
    private val logger = Logger.getLogger(javaClass.name)

    fun apply(preserveThreshold: Double, ratioThreshold: Double): Graph {
        val conflicts = findConflicts(graph)
        val edgesToRemove = conflicts.flatMap { (a, b) ->
            val ab = relativeSignificance(a, b)
            val ba = relativeSignificance(b, a)
            val offset = abs(ab - ba)

            logger.log(Level.FINER, "Rel. significance: $ab, $ba")
            if (significantlyGreater(ab, preserveThreshold) && significantlyGreater(ba, preserveThreshold)) {
                handleLength2Loop(a, b)
            } else if (offset >= ratioThreshold) {
                if (significantlyGreater(ab, ba)) {
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

    private fun relativeSignificance(a: Node, b: Node): Double {
        val abSignificance = (graph.edgeBetween(a, b)?.significance ?: 0.0) * 0.5
        val axSignificance = graph.nodes.sumByDouble { graph.edgeBetween(a, it)?.significance ?: 0.0 }
        val xbSignificance = graph.nodes.sumByDouble { graph.edgeBetween(it, b)?.significance ?: 0.0 }
        return 0.5 * abSignificance * ((1 / axSignificance) + (1 / xbSignificance))
    }
}

fun findConflicts(graph: Graph): NodePairs {
    return graph.nodes.flatMap { source ->
        graph.edgesFrom(source)
            .filter { edge -> graph.edgeBetween(edge.target, source) != null && edge.target != source }
            .map { edge -> sortedPair<Node>(source, edge.target) }
    }.toSet()
}

fun <T>sortedPair(a: T, b: T): Pair<T, T> {
    return if (a.toString() <= b.toString()) {
        a to b
    } else {
        b to a
    }
}