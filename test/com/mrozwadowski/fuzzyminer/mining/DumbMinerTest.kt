package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.createSimpleLog
import org.deckfour.xes.classification.XEventNameClassifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class DumbMinerTest {
    private val log = createSimpleLog(listOf("abd", "acd"))
    private val miner = DumbMiner(log, XEventNameClassifier())

    @Test
    fun mine() {
        val graph = miner.mine()
        assertEquals(4, graph.nodes.size)
        val nodeA = graph.nodes.find { it.eventClass.id == "a" }!!
        val nodeB = graph.nodes.find { it.eventClass.id == "b" }!!
        val nodeC = graph.nodes.find { it.eventClass.id == "c" }!!
        val nodeD = graph.nodes.find { it.eventClass.id == "d" }!!

        assertEquals(4, graph.allEdges().size)
        assertEquals(2, graph.edgesFrom(nodeA).size)
        assertEquals(1, graph.edgesFrom(nodeB).size)
        assertEquals(1, graph.edgesFrom(nodeC).size)
        assertEquals(0, graph.edgesFrom(nodeD).size)
        assertNotNull(graph.edgeBetween(nodeA, nodeB))
        assertNotNull(graph.edgeBetween(nodeA, nodeC))
        assertNotNull(graph.edgeBetween(nodeB, nodeD))
        assertNotNull(graph.edgeBetween(nodeC, nodeD))
    }
}