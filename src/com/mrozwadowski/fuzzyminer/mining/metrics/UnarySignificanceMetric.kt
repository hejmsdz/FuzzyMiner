package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.classification.XEventClass

abstract class UnarySignificanceMetric {
    abstract fun calculate(eventClass: XEventClass): Double
}
