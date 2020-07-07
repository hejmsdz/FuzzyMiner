package com.mrozwadowski.fuzzyminer.mining.metrics

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
                inValue += binarySignificance.getOrDefault(node2 to node1, 0.0)
                outValue += binarySignificance.getOrDefault(node1 to node2, 0.0)
            }
            if (inValue > 0.0 && outValue > 0.0) {
                values[node1] = ((inValue - outValue) / (inValue + outValue)).absoluteValue
            }
        }
    }
}
