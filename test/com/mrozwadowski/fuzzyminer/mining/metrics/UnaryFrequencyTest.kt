package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.createSimpleLog
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventNameClassifier
import org.junit.jupiter.api.Assertions.assertEquals
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
        val metrics = MetricsStore(mapOf(UnaryFrequency() to 1.0), mapOf(), mapOf(), null)
        metrics.calculateFromLog(log, classes)
        val significance = metrics.aggregateUnarySignificance

        assertEquals(0.75, significance[a])
        assertEquals(1.0, significance[b])
        assertEquals(0.25, significance[c])
        assertEquals(0.5, significance[d])
    }
}