package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.createLog
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventNameClassifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

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
    private val classes = XEventClasses.deriveEventClasses(XEventNameClassifier(), log)

    private val aClass = classes.getByIdentity(a)
    private val bClass = classes.getByIdentity(b)
    private val cClass = classes.getByIdentity(c)
    private val dClass = classes.getByIdentity(d)

    @Test
    fun calculate() {
        val metrics = MetricsStore(mapOf(), mapOf(), mapOf(EndpointCorrelation() to 1.0))
        metrics.calculateFromLog(log, classes)
        val correlation = metrics.aggregateBinaryCorrelation

        assertEquals(0.5625, correlation[aClass to bClass]!!, 0.0001)
        assertEquals(0.2941, correlation[aClass to cClass]!!, 0.0001)
        assertEquals(1.0000, correlation[bClass to bClass]!!, 0.0001)
        assertEquals(0.1250, correlation[bClass to dClass]!!, 0.0001)
    }
}