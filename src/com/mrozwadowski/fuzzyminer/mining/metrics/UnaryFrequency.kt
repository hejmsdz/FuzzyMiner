package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import com.mrozwadowski.fuzzyminer.data.log.Log

class UnaryFrequency<EventClass>(
    private val log: Log,
    private val classifier: Classifier<EventClass>
): UnarySignificanceMetric<EventClass>() {
    private val values = mutableMapOf<EventClass, Int>()

    init {
        log.traces
            .flatMap { it.events }
            .groupingBy(classifier)
            .eachCountTo(values)
    }

    override fun calculate(eventClass: EventClass): Number = values.getOrDefault(eventClass, 0)
}