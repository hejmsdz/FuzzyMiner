package com.mrozwadowski.fuzzyminer.mining.online

import com.mrozwadowski.fuzzyminer.createSimpleLog
import com.mrozwadowski.fuzzyminer.mining.metrics.minimalMetrics
import org.deckfour.xes.classification.XEventNameClassifier
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class OnlineFuzzyMinerTest {
    private val log1 = createSimpleLog(listOf("abe", "abf"))
    private val log2 = createSimpleLog(listOf("abce", "abde", "abdg"))

    @Test
    fun getEventClasses() {
        val onlineMiner = OnlineFuzzyMiner(XEventNameClassifier(), minimalMetrics())
        onlineMiner.learn(log1)
        val eventClasses = onlineMiner.getEventClasses(log2)
        assertEquals(7, eventClasses.classes.size)
    }
}