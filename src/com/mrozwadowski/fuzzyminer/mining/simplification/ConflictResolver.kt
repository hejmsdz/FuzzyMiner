package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node

class ConflictResolver(private val graph: Graph) {
    fun findConflicts(): Collection<Set<Node>> {
        return graph.nodes.flatMap { source ->
            graph.edgesFrom(source)
                .filter { edge -> graph.edgeBetween(edge.target, source) != null }
                .map { edge -> setOf(source, edge.target) }
        }.toSet()
    }

    /*
    fun relativeSignificance(a: Activity, b: Activity): Double {
        val abSignificance = binSignificance.calculate(a, b).toDouble() * 0.5
        val axSignificance = log.activities.sumByDouble { binSignificance.calculate(a, it).toDouble() }
        val xbSignificance = log.activities.sumByDouble { binSignificance.calculate(it, b).toDouble() }
        return 0.5 * abSignificance * ((1 / axSignificance) + (1 / xbSignificance))
    }
    */
}