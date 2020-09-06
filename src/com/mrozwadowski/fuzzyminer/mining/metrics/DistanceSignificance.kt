package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.utils.significantlyGreater
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
            if (significantlyGreater(aSignificance + bSignificance, 0.0) && significantlyGreater(abSignificance, 0.0)) {
                values[eventClasses] = 2 * (abSignificance / (aSignificance + bSignificance))
            }
        }
    }
}