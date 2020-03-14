package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.conceptName
import com.mrozwadowski.fuzzyminer.createSimpleLog
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class BinaryFrequencyTest {
    private val log = createSimpleLog(listOf("abd", "ac", "abbbd"))

    @Test
    fun calculate() {
        val metric = BinaryFrequency(log, ::conceptName)
        assertEquals(1.0, metric.calculate("a", "b"))
        assertEquals(0.5, metric.calculate("a", "c"))
        assertEquals(1.0, metric.calculate("b", "b"))
        assertEquals(1.0, metric.calculate("b", "d"))
        assertEquals(0.0, metric.calculate("a", "d"))
    }
}