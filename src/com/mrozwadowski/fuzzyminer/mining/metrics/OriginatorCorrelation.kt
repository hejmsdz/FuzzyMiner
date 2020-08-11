package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.model.XAttributeLiteral
import org.deckfour.xes.model.XEvent

class OriginatorCorrelation: LogBasedBinaryMetric(normalize = true) {
    override fun evaluate(previousEvent: XEvent, event: XEvent): Double {
        val resource1 = previousEvent.attributes["org:resource"] ?: return 0.0
        val resource2 = event.attributes["org:resource"] ?: return 0.0

        return levenshteinRatio(
            (resource1 as XAttributeLiteral).value,
            (resource2 as XAttributeLiteral).value
        )
    }
}