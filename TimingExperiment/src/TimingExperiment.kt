package com.mrozwadowski.fuzzyminer.timingExperiment

import com.mrozwadowski.fuzzyminer.experiments.SlidingWindow
import com.mrozwadowski.fuzzyminer.experiments.metricsFactory
import com.mrozwadowski.fuzzyminer.input.getLogReader
import com.mrozwadowski.fuzzyminer.mining.FuzzyMiner
import com.mrozwadowski.fuzzyminer.mining.online.OnlineFuzzyMiner
import org.deckfour.xes.classification.XEventNameClassifier
import java.io.File
import java.lang.management.ManagementFactory

fun main() {
    val logFiles = File("experimentData").listFiles()
    logFiles?.forEach { logFile ->
        println(logFile)

        (10..50 step 10).forEach { windowSize ->
            listOf(windowSize / 5, windowSize / 2, 3 * windowSize / 5).forEach { stride ->
                println("$windowSize : $stride")
                OnlineWindowComparison(logFile, windowSize, stride).run()
            }
        }
    }
}

class OnlineWindowComparison(private val logFile: File, private val windowSize: Int, private val stride: Int) {
    val log = getLogReader(logFile).readLog()
    val window = SlidingWindow(log, windowSize, stride)
    val classifier = XEventNameClassifier()

    fun run() {
        val onlineMetrics = metricsFactory()
        val onlineMiner = OnlineFuzzyMiner(classifier, onlineMetrics)

        onlineMiner.learn(window.initial())
        window.steps().forEach { step ->
            val onlineTime = benchmark {
                onlineMiner.learn(window.incoming(step))
                onlineMiner.unlearn(window.outgoing(step))
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
        println("${logFile.name},$windowSize,$stride,$step,$onlineTime,$offlineTime")
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