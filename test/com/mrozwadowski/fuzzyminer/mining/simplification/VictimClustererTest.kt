package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import org.deckfour.xes.classification.XEventClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

internal class VictimClustererTest {
    private val a = PrimitiveNode(XEventClass("a", 1), 0.5)
    private val b1 = PrimitiveNode(XEventClass("b1", 2), 0.2)
    private val c1 = PrimitiveNode(XEventClass("c1", 3), 0.1)
    private val c2 = PrimitiveNode(XEventClass("c2", 4), 0.25)

    private val graph = Graph(
        listOf(a, b1, c1, c2),
        mapOf(
            a to listOf(Edge(a, b1), Edge(a, c1)),
            c1 to listOf(Edge(c1, c2))
        )
    )

    private val victimClusterer = VictimClusterer(graph)

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