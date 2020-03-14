package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.createSimpleLog
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventNameClassifier
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class BinaryFrequencyTest {
    private val log = createSimpleLog(listOf("abd", "ac", "abbbd"))
    private val classes = XEventClasses.deriveEventClasses(XEventNameClassifier(), log)

    private val a = classes.getByIdentity("a")
    private val b = classes.getByIdentity("b")
    private val c = classes.getByIdentity("c")
    private val d = classes.getByIdentity("d")

    @Test
    fun calculate() {
        val metric = BinaryFrequency(log, classes)
        assertEquals(1.0, metric.calculate(a, b))
        assertEquals(0.5, metric.calculate(a, c))
        assertEquals(1.0, metric.calculate(b, b))
        assertEquals(1.0, metric.calculate(b, d))
        assertEquals(0.0, metric.calculate(a, d))
    }
}