package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Event
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.data.log.Trace
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class EndpointCorrelationTest {
    private val a = Activity("request analysis", 0)
    private val b = Activity("request approval", 1)
    private val c = Activity("requirement check", 2)
    private val d = Activity("quality check", 3)
    private val log = Log(listOf(
        Trace(listOf(Event(a), Event(b), Event(d))),
        Trace(listOf(Event(a), Event(c))),
        Trace(listOf(Event(a), Event(b), Event(b), Event(b), Event(d)))
    ))

    @Test
    fun calculate() {
        val ec = EndpointCorrelation(log, Event::activity)
        assertEquals(0.5625, ec.calculate(a, b), 0.0001)
        assertEquals(0.2941, ec.calculate(a, c), 0.0001)
        assertEquals(0.1250, ec.calculate(a, d), 0.0001)
    }

    @Test
    fun levenshtein() {
        assertEquals(0, EndpointCorrelation.levenshtein("banana", "banana"))
        assertEquals(3, EndpointCorrelation.levenshtein("kitten", "sitting"))
        assertEquals(5, EndpointCorrelation.levenshtein("intention", "execution"))
    }
}