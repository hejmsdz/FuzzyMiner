package com.mrozwadowski.fuzzyminer.experiments

import org.deckfour.xes.factory.XFactoryNaiveImpl
import org.deckfour.xes.model.XLog

class SlidingWindow(
    private val log: XLog,
    private val windowSize: Int,
    private val stride: Int
) {
    private val factory = XFactoryNaiveImpl()

    fun initial(): XLog {
        return logFragment(0, windowSize)
    }

    fun incoming(step: Int): XLog {
        return logFragment(windowSize + (step - 1) * stride, stride)
    }

    fun outgoing(step: Int): XLog {
        return logFragment((step - 1) * stride, stride)
    }

    fun steps(): IntRange {
        val lastStep = (log.size - windowSize) / stride
        return 1..lastStep
    }

    fun logFragment(position: Int, length: Int): XLog {
        val traces = log.subList(position, position + length)
        val fragment = factory.createLog(log.attributes)
        fragment.addAll(traces)
        return fragment
    }
}
