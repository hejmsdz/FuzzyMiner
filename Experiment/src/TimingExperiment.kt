package com.mrozwadowski.fuzzyminer.timingExperiment

import com.mrozwadowski.fuzzyminer.experiments.SlidingWindow
import com.mrozwadowski.fuzzyminer.experiments.metricsFactory
import com.mrozwadowski.fuzzyminer.mining.FuzzyMiner
import com.mrozwadowski.fuzzyminer.mining.online.OnlineFuzzyMiner
import org.deckfour.xes.classification.XEventNameClassifier
import org.deckfour.xes.model.XLog
import java.io.File
import java.io.IOException
import java.lang.management.ManagementFactory

fun main() {
    val logFiles = getLogFiles()
    (1..4).forEach { i ->
        val dao = TimingCsvDao("./results$i.csv")
        try {
            experimentLoop(logFiles) { log, windowSize, stride, logName ->
                val exp = OnlineWindowComparison(dao, log, logName, windowSize, stride)
                (1..5).forEach { exp.run() }
            } || return
        } finally {
            dao.close()
        }
    }
}

class TimingCsvDao(path: String): CsvDao(listOf("onlineTime", "offlineTime"), path)

class OnlineWindowComparison(private val dao: CsvDao?, log: XLog, private val logName: String, private val windowSize: Int, private val stride: Int) {
    private val window = SlidingWindow(log, windowSize, stride)
    private val classifier = XEventNameClassifier()

    fun run() {
        val onlineMetrics = metricsFactory()
        val onlineMiner = OnlineFuzzyMiner(classifier, onlineMetrics)

        onlineMiner.learn(window.initial())
        window.steps().forEach { step ->
            val onlineTime = benchmark {
                onlineMiner.learnUnlearn(window.incoming(step), window.outgoing(step))
                onlineMiner.graph
            }

            val fragment = window.fragment(step)
            val offlineMetrics = metricsFactory()
            val offlineMiner = FuzzyMiner(fragment, classifier, offlineMetrics)
            val offlineTime = benchmark {
                offlineMiner.mine()
            }

            reportTime(step, onlineTime, offlineTime)
        }
    }

    private fun reportTime(step: Int, onlineTime: Long, offlineTime: Long) {
        dao?.insert(logName, windowSize, stride, step, listOf(onlineTime, offlineTime))
    }
}

fun benchmark(function: () -> Unit): Long {
    val threadId = Thread.currentThread().id
    val mxb = ManagementFactory.getThreadMXBean()

    val startTime = mxb.getThreadCpuTime(threadId)
    function()
    val endTime = mxb.getThreadCpuTime(threadId)

    return endTime - startTime
}