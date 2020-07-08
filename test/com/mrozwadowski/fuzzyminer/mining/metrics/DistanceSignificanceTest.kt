package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.createSimpleLog
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventNameClassifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DistanceSignificanceTest {
    private val log = createSimpleLog(listOf("abd", "ac", "abbbd"))
    private val classes = XEventClasses.deriveEventClasses(XEventNameClassifier(), log)

    private val a = classes.getByIdentity("a")
    private val b = classes.getByIdentity("b")
    private val c = classes.getByIdentity("c")
    private val d = classes.getByIdentity("d")

    @Test
    fun calculate() {
        val metric = DistanceSignificance()
        val metrics = MetricsStore(
            mapOf(UnaryFrequency() to 1.0),
            mapOf(BinaryFrequency() to 0.6, metric to 0.4),
            mapOf(),
            null
        )
        metrics.calculateFromLog(log, classes)
        val distanceSignificance = metric.values
        val significance = metrics.aggregateBinarySignificance

        assertEquals(1.1428, distanceSignificance[a to b]!!, 0.0001)
        assertEquals(1.0000, distanceSignificance[a to c]!!, 0.0001)
        assertEquals(1.0000, distanceSignificance[b to b]!!, 0.0001)
        assertEquals(1.3333, distanceSignificance[b to d]!!, 0.0001)

        assertEquals(0.9428, significance[a to b]!!, 0.0001)
        assertEquals(0.6000, significance[a to c]!!, 0.0001)
        assertEquals(0.9000, significance[b to b]!!, 0.0001)
        assertEquals(1.0000, significance[b to d]!!, 0.0001)
    }
}