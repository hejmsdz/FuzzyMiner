package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.model.XLog

class BinaryFrequency(
    log: XLog,
    private val classes: XEventClasses
): BinaryMetric {
    private val values = mutableMapOf<Pair<XEventClass, XEventClass>, Int>()
    private var max = 0.0

    init {
        log.forEach { trace ->
            (1 until trace.size)
                .map { classes.getClassOf(trace[it - 1]) to classes.getClassOf(trace[it]) }
                .groupingBy { it }
                .eachCountTo(values)
        }
        max = values.values.max()?.toDouble() ?: 1.0
    }

    override fun calculate(class1: XEventClass, class2: XEventClass): Double {
        return values.getOrDefault(class1 to class2, 0) / max
    }

    fun allPairs() = values.keys
}