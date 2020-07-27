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
        return fragment(0)
    }

    fun fragment(step: Int): XLog {
        return logSlice(step * stride, windowSize)
    }

    fun incoming(step: Int): XLog {
        return logSlice(windowSize + (step - 1) * stride, stride)
    }

    fun outgoing(step: Int): XLog {
        return logSlice((step - 1) * stride, stride)
    }

    fun steps(): IntRange {
        val lastStep = (log.size - windowSize) / stride
        return 1..lastStep
    }

    private fun logSlice(position: Int, length: Int): XLog {
        val traces = log.subList(position, position + length)
        val fragment = factory.createLog(log.attributes)
        fragment.addAll(traces)
        return fragment
    }
}
