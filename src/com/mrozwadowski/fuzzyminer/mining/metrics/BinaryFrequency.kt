package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import com.mrozwadowski.fuzzyminer.data.log.Log

class BinaryFrequency<EventClass>(
    private val log: Log,
    private val classifier: Classifier<EventClass>
): BinarySignificanceMetric<EventClass>() {
    private val values = mutableMapOf<Pair<EventClass, EventClass>, Int>()
    private var max = 0.0

    init {
        log.traces.forEach { trace ->
            (1 until trace.events.size)
                .map { classifier(trace.events[it - 1]) to classifier(trace.events[it]) }
                .groupingBy { it }
                .eachCountTo(values)
        }
        max = values.values.max()?.toDouble() ?: 1.0
    }

    override fun calculate(class1: EventClass, class2: EventClass): Double {
        return values.getOrDefault(class1 to class2, 0) / max
    }

    fun allPairs() = values.keys
}