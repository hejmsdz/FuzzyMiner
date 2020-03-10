package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Event
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.data.log.Trace
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class BinaryFrequencyTest {
    private val a = Activity("a", 0)
    private val b = Activity("b", 1)
    private val c = Activity("c", 2)
    private val d = Activity("d", 3)
    private val log = Log(listOf(
        Trace(listOf(Event(a), Event(b), Event(d))),
        Trace(listOf(Event(a), Event(c))),
        Trace(listOf(Event(a), Event(b), Event(b), Event(b), Event(d)))
    ))

    @Test
    fun calculate() {
        val metric = BinaryFrequency(log, Event::activity)
        assertEquals(1.0, metric.calculate(a, b))
        assertEquals(0.5, metric.calculate(a, c))
        assertEquals(1.0, metric.calculate(b, b))
        assertEquals(1.0, metric.calculate(b, d))
        assertEquals(0.0, metric.calculate(a, d))
    }
}