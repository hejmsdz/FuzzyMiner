package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.model.XEvent

class UnaryFrequency: LogBasedUnaryMetric() {
    override fun evaluate(event: XEvent) = 1.0
}
