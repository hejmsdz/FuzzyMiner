package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Event
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.data.log.Trace
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DumbMinerTest {
    private val a = Activity("a", 0)
    private val b = Activity("b", 1)
    private val c = Activity("c", 2)
    private val d = Activity("d", 3)
    private val log = Log(listOf(
        Trace(listOf(Event(a), Event(b), Event(d))),
        Trace(listOf(Event(a), Event(c), Event(d)))
    ))
    private val miner = DumbMiner(log, Event::activity)

    @Test
    fun mine() {
        val graph = miner.mine()
        assertEquals(4, graph.nodes.size)
        val nodeA = graph.nodes.find { it.eventClass == a }!!
        val nodeB = graph.nodes.find { it.eventClass == b }!!
        val nodeC = graph.nodes.find { it.eventClass == c }!!
        val nodeD = graph.nodes.find { it.eventClass == d }!!

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