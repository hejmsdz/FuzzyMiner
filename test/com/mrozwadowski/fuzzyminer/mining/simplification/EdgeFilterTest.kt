package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.EdgeMetric
import org.deckfour.xes.classification.XEventClass
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class EdgeFilterTest {
    private val a = PrimitiveNode(XEventClass("a", 1))
    private val p = PrimitiveNode(XEventClass("p", 2))
    private val q = PrimitiveNode(XEventClass("q", 3))
    private val r = PrimitiveNode(XEventClass("r", 4))
    private val s = PrimitiveNode(XEventClass("s", 5))
    private val graph = Graph(
        listOf(a, p, q, r, s),
        mapOf(a to listOf(Edge(a, p), Edge(a, q), Edge(a, r), Edge(a, s)))
    )

    private class MockSignificance: BinaryMetric {
        val values = mapOf("p" to 1.0, "q" to 0.9, "r" to 0.7, "s" to 0.5)
        override fun calculate(class1: XEventClass, class2: XEventClass): Double {
            assert(class1.id == "a")
            return values.getOrDefault(class2.id, 0.0)
        }
    }

    private class MockCorrelation: BinaryMetric {
        val values = mapOf("p" to 1.0, "q" to 0.2, "r" to 0.8, "s" to 0.3)
        override fun calculate(class1: XEventClass, class2: XEventClass): Double {
            assert(class1.id == "a")
            return values.getOrDefault(class2.id, 0.0)
        }
    }

    private val edgeFilter = EdgeFilter(graph, EdgeMetric(MockSignificance()), EdgeMetric(MockCorrelation()))
    private val utilityRatio = 0.5
    private val cutoff = 0.4

    @Test
    fun relativeUtilities() {
        val values = edgeFilter.relativeUtilities(graph.edgesFrom(a), utilityRatio)
        assertEquals(1.000, values.getValue(Edge(a, p)), 0.001)
        assertEquals(0.250, values.getValue(Edge(a, q)), 0.001)
        assertEquals(0.583, values.getValue(Edge(a, r)), 0.001)
        assertEquals(0.000, values.getValue(Edge(a, s)), 0.001)
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