package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.model.XEvent

class BinaryFrequency: LogBasedBinaryMetric(normalize = false) {
    override fun evaluate(previousEvent: XEvent, event: XEvent) = 1.0
}