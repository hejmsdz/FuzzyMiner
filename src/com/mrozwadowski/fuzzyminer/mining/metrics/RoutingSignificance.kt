package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.utils.significantlyGreater
import org.deckfour.xes.classification.XEventClass
import kotlin.math.absoluteValue

class RoutingSignificance: DerivedUnaryMetric() {
    override fun calculate(
        unarySignificance: Map<XEventClass, Double>,
        binarySignificance: Map<Pair<XEventClass, XEventClass>, Double>,
        binaryCorrelation: Map<Pair<XEventClass, XEventClass>, Double>
    ) {
        val nodes = unarySignificance.keys
        nodes.forEach { node1 ->
            var inValue = 0.0
            var outValue = 0.0
            nodes.filter { it != node1 }.forEach { node2 ->
                val inTerm = (binarySignificance[node2 to node1] ?: 0.0) * (binaryCorrelation[node2 to node1] ?: 0.0)
                val outTerm = (binarySignificance[node1 to node2] ?: 0.0) * (binaryCorrelation[node1 to node2] ?: 0.0)
                if (significantlyGreater(inTerm, 0.0)) inValue += inTerm
                if (significantlyGreater(outTerm, 0.0)) outValue += outTerm
            }
            if (significantlyGreater(inValue, 0.0) && significantlyGreater(outValue, 0.0)) {
                values[node1] = ((inValue - outValue) / (inValue + outValue)).absoluteValue
            }
        }
    }
}
