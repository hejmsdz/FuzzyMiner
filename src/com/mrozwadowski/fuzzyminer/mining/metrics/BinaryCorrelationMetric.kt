package com.mrozwadowski.fuzzyminer.mining.metrics

abstract class BinaryCorrelationMetric<EventClass> {
    abstract fun calculate(class1: EventClass, class2: EventClass): Number
}
