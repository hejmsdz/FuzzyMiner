package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.conceptName
import com.mrozwadowski.fuzzyminer.createLog
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class EndpointCorrelationTest {
    private val a = "request analysis"
    private val b = "request approval"
    private val c = "requirement check"
    private val d = "quality check"

    private val log = createLog(listOf(
        listOf(a, b, d),
        listOf(a, c),
        listOf(a, b, b, b, d)
    ))

    @Test
    fun calculate() {
        val ec = EndpointCorrelation(log, ::conceptName)
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