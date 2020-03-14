package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import org.deckfour.xes.model.XLog

class UnaryFrequency<EventClass>(
    private val log: XLog,
    private val classifier: Classifier<EventClass>
): UnarySignificanceMetric<EventClass>() {
    private val values = mutableMapOf<EventClass, Int>()
    private var max = 0.0

    init {
        log.flatten()
            .groupingBy(classifier)
            .eachCountTo(values)
        max = values.values.max()?.toDouble() ?: 1.0
    }

    override fun calculate(eventClass: EventClass): Double = values.getOrDefault(eventClass, 0) / max
}