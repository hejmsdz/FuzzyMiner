package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import org.deckfour.xes.model.XLog

class BinaryFrequency<EventClass>(
    private val log: XLog,
    private val classifier: Classifier<EventClass>
): BinarySignificanceMetric<EventClass>() {
    private val values = mutableMapOf<Pair<EventClass, EventClass>, Int>()
    private var max = 0.0

    init {
        log.forEach { trace ->
            (1 until trace.size)
                .map { classifier(trace[it - 1]) to classifier(trace[it]) }
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