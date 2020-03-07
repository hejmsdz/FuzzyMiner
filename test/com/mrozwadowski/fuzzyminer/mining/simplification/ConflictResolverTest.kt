package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ConflictResolverTest {
    private val a = Node("a", 1)
    private val b = Node("b", 2)
    private val c = Node("c", 3)
    private val d = Node("d", 4)
    private val graph = Graph(
        listOf(a, b, c, d),
        mapOf(
            a to listOf(Edge(b), Edge(d)),
            b to listOf(Edge(c)),
            c to listOf(Edge(b), Edge(d)),
            d to listOf(Edge(a))
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