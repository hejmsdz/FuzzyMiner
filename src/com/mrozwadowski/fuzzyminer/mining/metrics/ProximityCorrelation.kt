package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.model.XAttributeTimestamp
import org.deckfour.xes.model.XEvent

class ProximityCorrelation: LogBasedBinaryMetric(normalize = true) {
    override fun evaluate(previousEvent: XEvent, event: XEvent): Double {
        if (!previousEvent.attributes.containsKey("time:timestamp") || !event.attributes.containsKey("time:timestamp")) {
            return 0.0
        }
        val timestamp0 = (previousEvent.attributes["time:timestamp"] as XAttributeTimestamp).valueMillis
        val timestamp1 = (event.attributes["time:timestamp"] as XAttributeTimestamp).valueMillis
        if (timestamp0 == timestamp1) {
            return 1.0
        }
        return 1.0 / (timestamp1 - timestamp0).toDouble()
    }
}