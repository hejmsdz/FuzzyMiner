package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Event
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.data.log.Trace
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UnaryFrequencyTest {
    private val a = Activity("a", 0)
    private val b = Activity("b", 1)
    private val c = Activity("c", 2)
    private val d = Activity("d", 3)
    private val e = Activity("e", 4)
    private val log = Log(listOf(
        Trace(listOf(Event(a), Event(b), Event(d))),
        Trace(listOf(Event(a), Event(c))),
        Trace(listOf(Event(a), Event(b), Event(b), Event(b), Event(d)))
    ))

    @Test
    fun calculate() {
        val metric = UnaryFrequency(log, Event::activity)
        assertEquals(0.75, metric.calculate(a))
        assertEquals(1.0, metric.calculate(b))
        assertEquals(0.25, metric.calculate(c))
        assertEquals(0.5, metric.calculate(d))
        assertEquals(0.0, metric.calculate(e))
    }
}