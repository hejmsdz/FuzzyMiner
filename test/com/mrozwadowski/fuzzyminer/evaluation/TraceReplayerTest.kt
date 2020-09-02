package com.mrozwadowski.fuzzyminer.evaluation

import com.mrozwadowski.fuzzyminer.createSimpleLog
import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.classification.XEventNameClassifier
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TraceReplayerTest {
    private val validTraces = listOf("abdef", "abcdf")
    private val invalidTraces = listOf(
        "abdcf", // invalid transition d -> c
        "abddf", // invalid transition d -> d
        "abfef", // invalid transition from final activity
        "bcdef", // invalid starting activity (should start with a)
        "agf", // unknown activity g
        "abd" // invalid final activity (should end with f)
    )
    private val validLog = createSimpleLog(validTraces)
    private val invalidLog = createSimpleLog(invalidTraces)
    private val completeLog = createSimpleLog(validTraces + invalidTraces)

    private val a = PrimitiveNode(XEventClass("a", 1), 1.0)
    private val b = PrimitiveNode(XEventClass("b", 2), 1.0)
    private val c = PrimitiveNode(XEventClass("c", 3), 0.5)
    private val d = PrimitiveNode(XEventClass("d", 4), 0.75)
    private val e = PrimitiveNode(XEventClass("e", 5), 0.5)
    private val f = PrimitiveNode(XEventClass("f", 6), 1.0)

    private val graph = Graph(
        listOf(a, b, c, d, e, f),
        mapOf(
            a to listOf(Edge(a, b, 1.0, 1.0)),
            b to listOf(
                Edge(b, c, 0.5, 0.0),
                Edge(b, d, 0.25, 0.36),
                Edge(b, f, 0.25, 0.14)
            ),
            c to listOf(Edge(c, d, 0.5, 0.24)),
            d to listOf(
                Edge(d, e, 0.5, 0.23),
                Edge(d, f, 0.25, 0.36)
            ),
            e to listOf(Edge(e, f, 0.5, 0.8))
        )
    )

    private val replayer = TraceReplayer(graph, XEventNameClassifier())

    @Test
    fun replayTrace() {
        validLog.forEach { assertEquals(1.0, replayer.replayTrace(it)) }

        assertEquals(1.0, replayer.replayTrace(invalidLog[0]), 0.001)
        assertEquals(0.8, replayer.replayTrace(invalidLog[1]), 0.001)
        assertEquals(0.8, replayer.replayTrace(invalidLog[2]), 0.001)
        assertEquals(0.8, replayer.replayTrace(invalidLog[3]), 0.001)
        assertEquals(0.333, replayer.replayTrace(invalidLog[4]), 0.001)
        assertEquals(0.667, replayer.replayTrace(invalidLog[5]), 0.001)
    }

    @Test
    fun replayLog() {
        assertEquals(0.733, replayer.replayLog(invalidLog), 0.001)
        assertEquals(1.0, replayer.replayLog(validLog))
        assertEquals(0.8, replayer.replayLog(completeLog), 0.001)
    }
}