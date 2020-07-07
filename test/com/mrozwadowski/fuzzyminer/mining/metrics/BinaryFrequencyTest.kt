package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.createSimpleLog
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventNameClassifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

internal class BinaryFrequencyTest {
    private val log = createSimpleLog(listOf("abd", "ac", "abbbd"))
    private val classes = XEventClasses.deriveEventClasses(XEventNameClassifier(), log)

    private val a = classes.getByIdentity("a")
    private val b = classes.getByIdentity("b")
    private val c = classes.getByIdentity("c")
    private val d = classes.getByIdentity("d")

    @Test
    fun calculate() {
        val metrics = MetricsStore(mapOf(), mapOf(BinaryFrequency() to 1.0), mapOf())
        metrics.calculateFromLog(log, classes)
        val significance = metrics.aggregateBinarySignificance

        assertEquals(1.0, significance[a to b])
        assertEquals(0.5, significance[a to c])
        assertEquals(1.0, significance[b to b])
        assertEquals(1.0, significance[b to d])
        assertNull(significance[a to d])
    }
}