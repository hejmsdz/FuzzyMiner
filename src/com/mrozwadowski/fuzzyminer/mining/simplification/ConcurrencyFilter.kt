package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.utils.significantlyGreater
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.absoluteValue

typealias NodePairs = Collection<Pair<Node, Node>>

private enum class ConflictResolution {
    Length2Loop,
    ABException,
    BAException,
    Concurrency
}

class ConcurrencyFilter(private val graph: Graph) {
    private val logger = Logger.getLogger(javaClass.name)

    fun apply(preserveThreshold: Double, ratioThreshold: Double): Graph {
        val conflicts = findConflicts(graph)
        val edgesToRemove = conflicts.flatMap { (a, b) ->
            val ab = relativeSignificance(a, b)
            val ba = relativeSignificance(b, a)

            logger.log(Level.FINER, "Rel. significance: $ab, $ba")
            when (resolveConflict(ab, ba, preserveThreshold, ratioThreshold)) {
                ConflictResolution.Length2Loop -> listOf()
                ConflictResolution.BAException -> listOf(b to a)
                ConflictResolution.ABException -> listOf(a to b)
                ConflictResolution.Concurrency -> listOf(a to b, b to a)
            }
        }

        return graph.withoutEdges(edgesToRemove)
    }

    private fun resolveConflict(ab: Double, ba: Double, preserveThreshold: Double, ratioThreshold: Double): ConflictResolution {
        val offset = (ab - ba).absoluteValue
        if (significantlyGreater(ab, preserveThreshold) && significantlyGreater(ba, preserveThreshold)) {
            return ConflictResolution.Length2Loop
        }
        if (significantlyGreater(offset, ratioThreshold)) {
            if (significantlyGreater(ab, ba)) {
                return ConflictResolution.BAException
            } else if (significantlyGreater(ba, ab)) {
                return ConflictResolution.ABException
            }
        }
        return ConflictResolution.Concurrency
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