package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Log

abstract class UnarySignificanceMetric(protected val log: Log) {
    abstract fun calculate(activity: Activity): Number
}
