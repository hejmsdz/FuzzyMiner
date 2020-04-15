package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.UnaryMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.EdgeMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.NodeMetric
import org.deckfour.xes.classification.XEventClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

internal class VictimClustererTest {
    private val a = PrimitiveNode(XEventClass("a", 1))
    private val b1 = PrimitiveNode(XEventClass("b1", 2))
    private val c1 = PrimitiveNode(XEventClass("c1", 3))
    private val c2 = PrimitiveNode(XEventClass("c2", 4))

    private val graph = Graph(
        listOf(a, b1, c1, c2),
        mapOf(
            a to listOf(Edge(a, b1), Edge(a, c1)),
            c1 to listOf(Edge(c1, c2))
        )
    )

    private class MockSignificance: UnaryMetric {
        val values = mapOf("a" to 0.5, "b1" to 0.2, "c1" to 0.1, "c2" to 0.25)
        override fun calculate(eventClass: XEventClass): Double {
            return values.getOrDefault(eventClass.id, 0.0)
        }
    }

    private class MockCorrelation: BinaryMetric {
        override fun calculate(class1: XEventClass, class2: XEventClass): Double = 0.0
    }

    private val victimClusterer = VictimClusterer(graph, NodeMetric(MockSignificance()), EdgeMetric(MockCorrelation()))

    @Test
    fun apply() {
        val simplifiedGraph = victimClusterer.apply(0.3)
        val nodes = simplifiedGraph.nodes
        assertEquals(3, nodes.size)
        assertTrue(a in nodes)

        val clusters = nodes.filterIsInstance<NodeCluster>()
        val b = clusters.find { it.nodes.size == 1 }!!
        val c = clusters.find { it.nodes.size == 2 }!!
        assertTrue(b1 in b.nodes)
        assertTrue(c1 in c.nodes)
        assertTrue(c2 in c.nodes)

        assertEquals(2, simplifiedGraph.allEdges().size)
        assertNotNull(simplifiedGraph.edgeBetween(a, b))
        assertNotNull(simplifiedGraph.edgeBetween(a, c))
    }
}