package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryCorrelationMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.BinarySignificanceMetric
import org.deckfour.xes.classification.XEventClass
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class EdgeFilterTest {
    private val a = Node(XEventClass("a", 1))
    private val p = Node(XEventClass("p", 2))
    private val q = Node(XEventClass("q", 3))
    private val r = Node(XEventClass("r", 4))
    private val s = Node(XEventClass("s", 5))
    private val graph = Graph(
        listOf(a, p, q, r, s),
        mapOf(a to listOf(Edge(a, p), Edge(a, q), Edge(a, r), Edge(a, s)))
    )

    private class MockSignificance: BinarySignificanceMetric() {
        val values = mapOf("p" to 1.0, "q" to 0.9, "r" to 0.7, "s" to 0.5)
        override fun calculate(class1: XEventClass, class2: XEventClass): Double {
            assert(class1.id == "a")
            return values.getOrDefault(class2.id, 0.0)
        }
    }

    private class MockCorrelation: BinaryCorrelationMetric() {
        val values = mapOf("p" to 1.0, "q" to 0.2, "r" to 0.8, "s" to 0.3)
        override fun calculate(class1: XEventClass, class2: XEventClass): Double {
            assert(class1.id == "a")
            return values.getOrDefault(class2.id, 0.0)
        }
    }

    private val edgeFilter = EdgeFilter(graph, MockSignificance(), MockCorrelation())
    private val utilityRatio = 0.5
    private val cutoff = 0.4

    @Test
    fun relativeUtilities() {
        val relativeUtilities = edgeFilter.relativeUtilities(a, graph.edgesFrom(a), utilityRatio)
        assertEquals(1.000, relativeUtilities[Edge(a, p)]!!, 0.001)
        assertEquals(0.250, relativeUtilities[Edge(a, q)]!!, 0.001)
        assertEquals(0.583, relativeUtilities[Edge(a, r)]!!, 0.001)
        assertEquals(0.000, relativeUtilities[Edge(a, s)]!!, 0.001)
    }

    @Test
    fun filterEdges() {
        val simplifiedGraph = edgeFilter.filterEdges(utilityRatio, cutoff)
        assertNotNull(simplifiedGraph.edgeBetween(a, p))
        assertNull(simplifiedGraph.edgeBetween(a, q))
        assertNotNull(simplifiedGraph.edgeBetween(a, r))
        assertNull(simplifiedGraph.edgeBetween(a, s))
    }
}