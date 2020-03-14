package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import org.deckfour.xes.classification.XEventClass
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ConcurrencyFilterTest {
    private val a = Node(XEventClass("a", 1))
    private val b = Node(XEventClass("b", 2))
    private val c = Node(XEventClass("c", 3))
    private val d = Node(XEventClass("d", 4))
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