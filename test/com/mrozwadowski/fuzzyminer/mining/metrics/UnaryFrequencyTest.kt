package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.createSimpleLog
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventNameClassifier
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class UnaryFrequencyTest {
    private val log = createSimpleLog(listOf("abd", "ac", "abbbd"))
    private val classes = XEventClasses.deriveEventClasses(XEventNameClassifier(), log)

    private val a = classes.getByIdentity("a")
    private val b = classes.getByIdentity("b")
    private val c = classes.getByIdentity("c")
    private val d = classes.getByIdentity("d")

    @Test
    fun calculate() {
        val metric = UnaryFrequency(log, classes)
        assertEquals(0.75, metric.calculate(a))
        assertEquals(1.0, metric.calculate(b))
        assertEquals(0.25, metric.calculate(c))
        assertEquals(0.5, metric.calculate(d))
    }
}