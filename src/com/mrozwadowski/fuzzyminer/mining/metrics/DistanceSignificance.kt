package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.classification.XEventClass

class DistanceSignificance: DerivedBinaryMetric() {
    override fun calculate(
        unarySignificance: Map<XEventClass, Double>,
        binarySignificance: Map<XEventClassPair, Double>,
        binaryCorrelation: Map<XEventClassPair, Double>
    ) {
        binarySignificance.forEach { (eventClasses, abSignificanceWeighted) ->
            val abSignificance = abSignificanceWeighted / logBasedBinarySignificanceWeight
            val aSignificance = (unarySignificance[eventClasses.first] ?: 0.0) / logBasedUnarySignificanceWeight
            val bSignificance = (unarySignificance[eventClasses.second] ?: 0.0) / logBasedUnarySignificanceWeight
            if (aSignificance > 0 && bSignificance > 0)
            values[eventClasses] = 2 * (abSignificance / (aSignificance + bSignificance))
        }
    }
}