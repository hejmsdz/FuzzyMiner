package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import org.deckfour.xes.classification.XEventClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ConcurrencyFilterTest {
    private val a = PrimitiveNode(XEventClass("a", 1))
    private val b = PrimitiveNode(XEventClass("b", 2))
    private val c = PrimitiveNode(XEventClass("c", 3))
    private val d = PrimitiveNode(XEventClass("d", 4))
    private val graph = Graph(
        listOf(a, b, c, d),
        mapOf(
            a to listOf(Edge(a, b), Edge(a, d)),
            b to listOf(Edge(b, c)),
            c to listOf(Edge(c, b), Edge(c, d)),
            d to listOf(Edge(d, a))
        )
    )

    @Test
    fun findConflicts() {
        val conflicts = findConflicts(graph)

        assertEquals(2, conflicts.size)
        assertTrue(setOf(a, d) in conflicts)
        assertTrue(setOf(b, c) in conflicts)
    }
}