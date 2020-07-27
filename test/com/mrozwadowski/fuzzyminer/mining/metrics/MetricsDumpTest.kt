package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.createSimpleLog
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventNameClassifier
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MetricsDumpTest {
    private val log = createSimpleLog(listOf("abd", "ac", "abbbd"))
    private val classes = XEventClasses.deriveEventClasses(XEventNameClassifier(), log)
    private val metrics = minimalMetrics()

    @Test
    fun dumpAndLoad() {
        metrics.calculateFromLog(log, classes)

        val dump = metrics.dumpMetrics()
        val jsonDump = dump.toString()
        val parsedDump = MetricsDump.fromString(jsonDump)

        assertNotNull(parsedDump)

        val loadedMetrics = minimalMetrics()
        loadedMetrics.loadMetrics(parsedDump!!)

        assertEquals(metrics.aggregateUnarySignificance.filterValues { it > 0 }, loadedMetrics.aggregateUnarySignificance.filterValues { it > 0 })
        assertEquals(metrics.aggregateBinarySignificance.filterValues { it > 0 }, loadedMetrics.aggregateBinarySignificance.filterValues { it > 0 })
        assertEquals(metrics.aggregateBinaryCorrelation.filterValues { it > 0 }, loadedMetrics.aggregateBinaryCorrelation.filterValues { it > 0 })
    }
}