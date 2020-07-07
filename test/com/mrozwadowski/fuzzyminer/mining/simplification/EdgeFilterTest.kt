package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import org.deckfour.xes.classification.XEventClass
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class EdgeFilterTest {
    private val a = PrimitiveNode(XEventClass("a", 1), 0.0)
    private val p = PrimitiveNode(XEventClass("p", 2), 1.0)
    private val q = PrimitiveNode(XEventClass("q", 3), 0.9)
    private val r = PrimitiveNode(XEventClass("r", 4), 0.7)
    private val s = PrimitiveNode(XEventClass("s", 5), 0.5)

    private val apEdge = Edge(a, p, 1.0, 1.0)
    private val aqEdge = Edge(a, q, 0.9, 0.2)
    private val arEdge = Edge(a, r, 0.7, 0.8)
    private val asEdge = Edge(a, s, 0.5, 0.3)

    private val graph = Graph(
        listOf(a, p, q, r, s),
        mapOf(a to listOf(apEdge, aqEdge, arEdge, asEdge))
    )

    private val edgeFilter = EdgeFilter(graph)
    private val utilityRatio = 0.5
    private val cutoff = 0.4

    @Test
    fun relativeUtilities() {
        val values = edgeFilter.relativeUtilities(graph.edgesFrom(a), utilityRatio)
        assertEquals(1.000, values.getValue(apEdge), 0.001)
        assertEquals(0.250, values.getValue(aqEdge), 0.001)
        assertEquals(0.583, values.getValue(arEdge), 0.001)
        assertEquals(0.000, values.getValue(asEdge), 0.001)
    }

    @Test
    fun apply() {
        val simplifiedGraph = edgeFilter.apply(utilityRatio, cutoff)
        assertNotNull(simplifiedGraph.edgeBetween(a, p))
        assertNull(simplifiedGraph.edgeBetween(a, q))
        assertNotNull(simplifiedGraph.edgeBetween(a, r))
        assertNull(simplifiedGraph.edgeBetween(a, s))
    }
}