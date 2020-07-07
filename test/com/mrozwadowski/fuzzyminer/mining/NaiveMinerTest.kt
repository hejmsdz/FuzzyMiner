package com.mrozwadowski.fuzzyminer.mining

/*
internal class NaiveMinerTest {
    private val log = createSimpleLog(listOf("abd", "acd"))
    private val miner = NaiveMiner(log, XEventNameClassifier())

    @Test
    fun mine() {
        val graph = miner.mine()
        assertEquals(4, graph.nodes.size)
        val nodeA = graph.nodes.find { it.toString() == "a" }!!
        val nodeB = graph.nodes.find { it.toString() == "b" }!!
        val nodeC = graph.nodes.find { it.toString() == "c" }!!
        val nodeD = graph.nodes.find { it.toString() == "d" }!!

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
 */