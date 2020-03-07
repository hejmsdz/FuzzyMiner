package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Log

class BinaryFrequency(log: Log): BinarySignificanceMetric(log) {
    private val values = mutableMapOf<Pair<Activity, Activity>, Int>()

    init {
        log.traces.forEach { trace ->
            (1 until trace.events.size)
                .map { trace.events[it - 1].activity to trace.events[it].activity }
                .groupingBy { it }
                .eachCountTo(values)
        }
    }

    override fun calculate(activity1: Activity, activity2: Activity): Number {
        return values.getOrDefault(activity1 to activity2, 0)
    }
}