package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.model.XLog

class UnaryFrequency(
    log: XLog,
    classes: XEventClasses
): UnaryMetric {
    private val values = mutableMapOf<XEventClass, Int>()
    private var max = 0.0

    init {
        log.flatten()
            .groupingBy(classes::getClassOf)
            .eachCountTo(values)
        max = values.values.max()?.toDouble() ?: 1.0
    }

    override fun calculate(eventClass: XEventClass): Double = values.getOrDefault(eventClass, 0) / max
}