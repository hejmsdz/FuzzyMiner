package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import com.mrozwadowski.fuzzyminer.data.log.Log

class BinaryFrequency<EventClass>(
    log: Log,
    classifier: Classifier<EventClass>
): BinarySignificanceMetric<EventClass>(log, classifier) {
    private val values = mutableMapOf<Pair<EventClass, EventClass>, Int>()

    init {
        log.traces.forEach { trace ->
            (1 until trace.events.size)
                .map { classifier(trace.events[it - 1]) to classifier(trace.events[it]) }
                .groupingBy { it }
                .eachCountTo(values)
        }
    }

    override fun calculate(class1: EventClass, class2: EventClass): Number {
        return values.getOrDefault(class1 to class2, 0)
    }

    fun allPairs() = values.keys
}