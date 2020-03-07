package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import com.mrozwadowski.fuzzyminer.data.log.Log

abstract class UnarySignificanceMetric<EventClass>(
    protected val log: Log,
    protected val classifier: Classifier<EventClass>
) {
    abstract fun calculate(eventClass: EventClass): Number
}
