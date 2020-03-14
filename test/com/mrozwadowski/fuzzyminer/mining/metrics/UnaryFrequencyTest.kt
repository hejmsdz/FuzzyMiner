package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.conceptName
import com.mrozwadowski.fuzzyminer.createSimpleLog
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UnaryFrequencyTest {
    private val log = createSimpleLog(listOf("abd", "ac", "abbbd"))

    @Test
    fun calculate() {
        val metric = UnaryFrequency(log, ::conceptName)
        assertEquals(0.75, metric.calculate("a"))
        assertEquals(1.0, metric.calculate("b"))
        assertEquals(0.25, metric.calculate("c"))
        assertEquals(0.5, metric.calculate("d"))
        assertEquals(0.0, metric.calculate("e"))
    }
}