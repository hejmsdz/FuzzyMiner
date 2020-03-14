package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.classification.XEventClass

abstract class BinaryCorrelationMetric {
    abstract fun calculate(class1: XEventClass, class2: XEventClass): Double
}
