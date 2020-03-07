package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Log

abstract class BinarySignificanceMetric(protected val log: Log) {
    abstract fun calculate(activity1: Activity, activity2: Activity): Number
}
