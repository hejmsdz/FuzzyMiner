package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Log

class UnaryFrequency(log: Log): UnarySignificanceMetric(log) {
    private val values = mutableMapOf<Activity, Int>()

    init {
        log.traces
            .flatMap { it.events }
            .groupingBy { it.activity }
            .eachCountTo(values)
    }

    override fun calculate(activity: Activity): Number = values.getOrDefault(activity, 0)
}