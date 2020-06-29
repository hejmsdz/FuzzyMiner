package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.classification.XEventClass

interface UnaryMetric {
    fun calculate(eventClass: XEventClass): Double
}
