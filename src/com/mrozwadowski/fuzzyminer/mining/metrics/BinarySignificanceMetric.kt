package com.mrozwadowski.fuzzyminer.mining.metrics

abstract class BinarySignificanceMetric<EventClass> {
    abstract fun calculate(class1: EventClass, class2: EventClass): Double
}
