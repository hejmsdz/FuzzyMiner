package com.mrozwadowski.fuzzyminer.evaluation

import com.mrozwadowski.fuzzyminer.createSimpleLog
import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.classification.XEventNameClassifier
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class PrecisionMeterTest {
    private val log = createSimpleLog(listOf("abcf", "abdf"))

    private val a = PrimitiveNode(XEventClass("a", 1), 1.0)
    private val b = PrimitiveNode(XEventClass("b", 2), 1.0)
    private val c = PrimitiveNode(XEventClass("c", 3), 0.5)
    private val d = PrimitiveNode(XEventClass("d", 4), 0.75)
    private val e = PrimitiveNode(XEventClass("e", 5), 0.5)
    private val f = PrimitiveNode(XEventClass("f", 6), 1.0)

    private val graph = Graph(
        listOf(a, b, c, d, e, f),
        mapOf(
            a to listOf(Edge(a, b)),
            b to listOf(
                Edge(b, c),
                Edge(b, d),
                Edge(b, e)
            ),
            c to listOf(Edge(c, f)),
            d to listOf(Edge(d, f)),
            e to listOf(Edge(e, f))
        )
    )

    private val classifier = XEventNameClassifier()
    private val precisionMeter = PrecisionMeter(graph, classifier)

    @Test
    fun calculate() {
        val precision = precisionMeter.calculate(log)
        assertEquals(0.888, precision, 0.001)
    }

    @Test
    fun successorsFromLog() {
        val cLog = classifyLog(log, classifier)
        val successors = precisionMeter.successorsFromLog(listOf("a", "b"), cLog)
        assertIterableEquals(setOf("c", "d"), successors)
    }

    @Test
    fun successorsFromGraph() {
        val successors = precisionMeter.successorsFromGraph(listOf("a", "b"))
        assertIterableEquals(setOf("c", "d", "e"), successors)
    }
}