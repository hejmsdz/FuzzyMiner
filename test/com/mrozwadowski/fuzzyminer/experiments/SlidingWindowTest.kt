package com.mrozwadowski.fuzzyminer.experiments

import com.mrozwadowski.fuzzyminer.createSimpleLog
import org.deckfour.xes.model.XAttributeLiteral
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class SlidingWindowTest {
    private val traces = listOf(
        "abdeh", "adceg", "acdefbdeg", "adbeh", "acdefdcefcdeh",
        "acdeg", "abeg", "abde", "acdefdcefbdeh"
    )
    private val log = createSimpleLog(traces)

    @Test
    fun iterate() {
        val window = SlidingWindow(log, 5, 2)

        val initial = logToStrings(window.initial())
        assertEquals(traces.slice(0..4), initial)

        val incoming = mutableListOf<List<String>>()
        val outgoing = mutableListOf<List<String>>()
        window.steps().forEach { k ->
            incoming.add(logToStrings(window.incoming(k)))
            outgoing.add(logToStrings(window.outgoing(k)))
        }

        assertEquals(2, incoming.size)
        assertEquals(2, outgoing.size)
        assertEquals(traces.slice(5..6), incoming[0])
        assertEquals(traces.slice(0..1), outgoing[0])
        assertEquals(traces.slice(7..8), incoming[1])
        assertEquals(traces.slice(2..3), outgoing[1])
    }

    private fun logToStrings(log: XLog): List<String> {
        return log.map { traceToString(it) }
    }

    private fun traceToString(trace: XTrace): String {
        return trace.joinToString("") { event ->
            (event.attributes["concept:name"] as XAttributeLiteral).value
        }
    }
}