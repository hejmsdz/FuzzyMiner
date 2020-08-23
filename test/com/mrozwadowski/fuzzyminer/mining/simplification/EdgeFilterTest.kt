package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.output.Dot
import org.deckfour.xes.classification.XEventClass
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class EdgeFilterTest {
    private val a = PrimitiveNode(XEventClass("a", 1), 0.0)
    private val p = PrimitiveNode(XEventClass("p", 2), 1.0)
    private val q = PrimitiveNode(XEventClass("q", 3), 0.9)
    private val r = PrimitiveNode(XEventClass("r", 4), 0.7)
    private val s = PrimitiveNode(XEventClass("s", 5), 0.5)

    private val paEdge = Edge(p, a, 1.0, 1.0)
    private val qaEdge = Edge(q, a,0.9, 0.2)
    private val raEdge = Edge(r, a, 0.7, 0.8)
    private val saEdge = Edge(s, a, 0.5, 0.3)

    private val graph = Graph(
        listOf(a, p, q, r, s),
        mapOf(a to listOf(paEdge, qaEdge, raEdge, saEdge))
    )

    private val edgeFilter = EdgeFilter(graph)
    private val utilityRatio = 0.5
    private val cutoff = 0.4

    @Test
    fun relativeUtilities() {
        val values = edgeFilter.relativeUtilities(graph.edgesTo(a), utilityRatio)
        assertEquals(1.000, values.getValue(paEdge), 0.001)
        assertEquals(0.250, values.getValue(qaEdge), 0.001)
        assertEquals(0.583, values.getValue(raEdge), 0.001)
        assertEquals(0.000, values.getValue(saEdge), 0.001)
    }

    @Test
    fun apply() {
        val simplifiedGraph = edgeFilter.apply(utilityRatio, cutoff)
        assertNotNull(simplifiedGraph.edgeBetween(p, a))
        assertNull(simplifiedGraph.edgeBetween(q, a))
        assertNotNull(simplifiedGraph.edgeBetween(r, a))
        assertNull(simplifiedGraph.edgeBetween(s, a))
    }
}