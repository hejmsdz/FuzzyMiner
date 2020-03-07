package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Log

abstract class BinaryCorrelationMetric(protected val log: Log) {
    abstract fun calculate(activity1: Activity, activity2: Activity): Number
}
