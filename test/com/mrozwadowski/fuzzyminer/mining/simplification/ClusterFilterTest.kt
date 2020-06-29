package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import org.deckfour.xes.classification.XEventClass
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ClusterFilterTest {
    private val a = NodeCluster(listOf(
        PrimitiveNode(XEventClass("a1", 1)),
        PrimitiveNode(XEventClass("a2", 2))
    ))
    private val b = NodeCluster(listOf(
        PrimitiveNode(XEventClass("b1", 3)),
        PrimitiveNode(XEventClass("b2", 4))
    ))
    private val c = NodeCluster(listOf(
        PrimitiveNode(XEventClass("c1", 5))
    ))
    private val d = NodeCluster(listOf(
        PrimitiveNode(XEventClass("d1", 6))
    ))
    private val e = NodeCluster(listOf(
        PrimitiveNode(XEventClass("e1", 7)),
        PrimitiveNode(XEventClass("e2", 8))
    ))
    private val x = PrimitiveNode(XEventClass("x", 9))

    private val graph = Graph(
        listOf(a, b, c, d, e, x),
        mapOf(
            a to listOf(Edge(a, c)),
            c to listOf(Edge(c, d)),
            d to listOf(Edge(d, e), Edge(d, x))
        )
    )

    private val clusterFilter = ClusterFilter(graph)

    @Test
    fun apply() {
        val simplifiedGraph = clusterFilter.apply()
        val (nodes) = simplifiedGraph

        assertEquals(3, nodes.size)
        assertTrue(a in nodes)
        assertTrue(e in nodes)
        assertTrue(x in nodes)
        assertEquals(2, simplifiedGraph.allEdges().size)
        assertNotNull(simplifiedGraph.edgeBetween(a, e))
        assertNotNull(simplifiedGraph.edgeBetween(a, x))
    }
}