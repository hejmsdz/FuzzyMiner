package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.model.XAttributeLiteral
import org.deckfour.xes.model.XEvent

class OriginatorCorrelation: LogBasedBinaryMetric(normalize = true) {
    override fun evaluate(previousEvent: XEvent, event: XEvent): Double {
        return levenshteinRatio(
            (previousEvent.attributes["org:resource"] as XAttributeLiteral).value,
            (event.attributes["org:resource"] as XAttributeLiteral).value
        )
    }
}