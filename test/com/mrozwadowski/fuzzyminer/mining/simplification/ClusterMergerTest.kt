package com.mrozwadowski.fuzzyminer.mining.simplification

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import org.deckfour.xes.classification.XEventClass
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class ClusterMergerTest {
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
    private val x = PrimitiveNode(XEventClass("x", 6))
    private val y = PrimitiveNode(XEventClass("y", 7))

    private val graph = Graph(
        listOf(a, b, c, x, y),
        mapOf(
            a to listOf(Edge(a, x), Edge(a, b)),
            x to listOf(Edge(x, b)),
            b to listOf(Edge(b, c)),
            c to listOf(Edge(c, y))
        )
    )

    private val clusterMerger = ClusterMerger(graph)

    @Test
    fun apply() {
        val simplifiedGraph = clusterMerger.apply()
        val (nodes) = simplifiedGraph

        assertTrue(x in nodes)
        assertTrue(y in nodes)
        assertTrue(a in nodes)
        val bc = nodes.filterIsInstance<NodeCluster>().find { it.nodes.size == 3 }
        assertNotNull(bc)
        assertEquals(4, simplifiedGraph.allEdges().size)
        assertNotNull(simplifiedGraph.edgeBetween(a, x))
        assertNotNull(simplifiedGraph.edgeBetween(a, bc!!))
        assertNotNull(simplifiedGraph.edgeBetween(bc!!, y))
    }
}