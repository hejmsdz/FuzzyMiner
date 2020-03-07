package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import com.mrozwadowski.fuzzyminer.data.log.Log

abstract class BinarySignificanceMetric<EventClass>(
    protected val log: Log,
    protected val classifier: Classifier<EventClass>
) {
    abstract fun calculate(class1: EventClass, class2: EventClass): Number
}
