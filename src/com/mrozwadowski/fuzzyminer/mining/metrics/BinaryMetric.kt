package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.classification.XEventClass

interface BinaryMetric {
    fun calculate(class1: XEventClass, class2: XEventClass): Double
}
