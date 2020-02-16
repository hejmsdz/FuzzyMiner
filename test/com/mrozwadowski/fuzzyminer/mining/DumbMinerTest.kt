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

    private val miner = DumbMiner(log)

    @Test
    fun mine() {
        val graph = miner.mine()
        assertEquals(4, graph.nodes.size)
        val nodeA = graph.nodes.find { it.name == a.name }!!
        val nodeB = graph.nodes.find { it.name == b.name }!!
        val nodeC = graph.nodes.find { it.name == c.name }!!
        val nodeD = graph.nodes.find { it.name == d.name }!!

        assertEquals(3, graph.edges.size)
        assertEquals(2, graph.edges[nodeA]?.size)
        assertEquals(1, graph.edges[nodeB]?.size)
        assertEquals(1, graph.edges[nodeC]?.size)
        assertNotNull(graph.edges[nodeA]?.find { it.target == nodeB })
        assertNotNull(graph.edges[nodeA]?.find { it.target == nodeC })
        assertNotNull(graph.edges[nodeB]?.find { it.target == nodeD })
        assertNotNull(graph.edges[nodeC]?.find { it.target == nodeD })

        println(graph.edges)
    }
}