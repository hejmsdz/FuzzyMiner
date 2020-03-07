package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryCorrelationMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.BinarySignificanceMetric
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class EdgeFilterTest {
    val a = Node(Activity("a", 1), 1)
    val p = Node(Activity("p", 2), 2)
    val q = Node(Activity("q", 3), 3)
    val r = Node(Activity("r", 4), 4)
    val s = Node(Activity("s", 5), 5)
    val graph = Graph(
        listOf(a, p, q, r, s),
        mapOf(a to listOf(Edge(p), Edge(q), Edge(r), Edge(s)))
    )

    private class MockSignificance: BinarySignificanceMetric<Activity>() {
        val values = mapOf("p" to 1.0, "q" to 0.9, "r" to 0.7, "s" to 0.5)
        override fun calculate(class1: Activity, class2: Activity): Number {
            assert(class1.name == "a")
            return values.getOrDefault(class2.name, 0)
        }
    }

    private class MockCorrelation: BinaryCorrelationMetric<Activity>() {
        val values = mapOf("p" to 1.0, "q" to 0.2, "r" to 0.8, "s" to 0.3)
        override fun calculate(class1: Activity, class2: Activity): Number {
            assert(class1.name == "a")
            return values.getOrDefault(class2.name, 0)
        }
    }

    private val edgeFilter = EdgeFilter(graph, MockSignificance(), MockCorrelation())
    private val utilityRatio = 0.5
    private val cutoff = 0.4

    @Test
    fun relativeUtilities() {
        val relativeUtilities = edgeFilter.relativeUtilities(a, graph.edgesFrom(a), utilityRatio)
        assertEquals(1.000, relativeUtilities[Edge(p)]!!, 0.001)
        assertEquals(0.250, relativeUtilities[Edge(q)]!!, 0.001)
        assertEquals(0.583, relativeUtilities[Edge(r)]!!, 0.001)
        assertEquals(0.000, relativeUtilities[Edge(s)]!!, 0.001)
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