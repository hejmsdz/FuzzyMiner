package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.model.XAttributeLiteral
import org.deckfour.xes.model.XEvent

class EndpointCorrelation: LogBasedBinaryMetric(normalize = true) {
    override fun evaluate(previousEvent: XEvent, event: XEvent): Double {
        return levenshteinRatio(
            (previousEvent.attributes["concept:name"] as XAttributeLiteral).value,
            (event.attributes["concept:name"] as XAttributeLiteral).value
        )
    }
}