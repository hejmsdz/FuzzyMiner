package com.mrozwadowski.fuzzyminer.mining.metrics

abstract class UnarySignificanceMetric<EventClass> {
    abstract fun calculate(eventClass: EventClass): Number
}
