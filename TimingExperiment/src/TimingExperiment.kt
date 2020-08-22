package com.mrozwadowski.fuzzyminer.timingExperiment

import com.mrozwadowski.fuzzyminer.experiments.SlidingWindow
import com.mrozwadowski.fuzzyminer.experiments.metricsFactory
import com.mrozwadowski.fuzzyminer.input.getLogReader
import com.mrozwadowski.fuzzyminer.mining.FuzzyMiner
import com.mrozwadowski.fuzzyminer.mining.online.OnlineFuzzyMiner
import org.deckfour.xes.classification.XEventNameClassifier
import org.deckfour.xes.model.XLog
import java.io.File
import java.lang.management.ManagementFactory

fun main() {
    val dao = CsvDao("./results.csv")
    val logFiles = File("experimentData").listFiles()?.slice(0 until 3)
    logFiles?.forEach { logFile ->
        val log = getLogReader(logFile).readLog()
        println("$logFile (${log.size} traces)")

        (20..200 step 20).forEach { windowSize ->
            (1..4).map { windowSize * it / 5 }.forEach { stride ->
                println("$windowSize : $stride")
                val exp = OnlineWindowComparison(dao, log, logFile.name, windowSize, stride)
                (1..5).forEach { exp.run() }
            }
        }
    }
    dao.close()
}

class CsvDao(path: String) {
    private val file = File(path)
    private val writer = file.printWriter()

    init {
        writer.println("log,windowSize,stride,step,onlineTime,offlineTime")
    }

    fun insert(logName: String, windowSize: Int, stride: Int, step: Int, onlineTime: Long, offlineTime: Long) {
        writer.println("$logName,$windowSize,$stride,$step,$onlineTime,$offlineTime")
    }

    fun close() {
        writer.close()
    }
}

class OnlineWindowComparison(private val dao: CsvDao?, log: XLog, private val logName: String, private val windowSize: Int, private val stride: Int) {
    private val window = SlidingWindow(log, windowSize, stride)
    private val classifier = XEventNameClassifier()

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
        dao?.insert(logName, windowSize, stride, step, onlineTime, offlineTime)
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