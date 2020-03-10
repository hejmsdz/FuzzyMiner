package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import com.mrozwadowski.fuzzyminer.data.log.Log

class UnaryFrequency<EventClass>(
    private val log: Log,
    private val classifier: Classifier<EventClass>
): UnarySignificanceMetric<EventClass>() {
    private val values = mutableMapOf<EventClass, Int>()
    private var max = 0.0

    init {
        log.traces
            .flatMap { it.events }
            .groupingBy(classifier)
            .eachCountTo(values)
        max = values.values.max()?.toDouble() ?: 1.0
    }

    override fun calculate(eventClass: EventClass): Double = values.getOrDefault(eventClass, 0) / max
}