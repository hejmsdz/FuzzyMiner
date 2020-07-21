package com.mrozwadowski.fuzzyminer.experiments

import com.mrozwadowski.fuzzyminer.input.getLogReader
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsStore
import com.mrozwadowski.fuzzyminer.mining.metrics.minimalMetrics
import com.mrozwadowski.fuzzyminer.mining.online.OnlineFuzzyMiner
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventNameClassifier
import java.io.File

fun main() {
    val log = getLogReader(File("sampleData/journal_review.xes")).readLog()
    val window = SlidingWindow(log, windowSize = 20, stride = 5)

    val classifier = XEventNameClassifier()
    val metrics = minimalMetrics()
    val onlineMiner = OnlineFuzzyMiner(classifier, metrics)

    onlineMiner.learn(window.initial())
    window.steps().forEach { step ->
        onlineMiner.learn(window.incoming(step))
        onlineMiner.unlearn(window.outgoing(step))
    }

    printMetrics(metrics)

    val lastFragment = window.fragment(window.steps().last)
    val offlineMetrics = minimalMetrics()
    offlineMetrics.calculateFromLog(lastFragment, XEventClasses.deriveEventClasses(classifier, lastFragment))
    printMetrics(offlineMetrics)
}

fun printMetrics(metrics: MetricsStore) {
    val dump = metrics.dumpMetrics()
    dump.unarySignificance.keys.sortedWith(compareBy({ it.metricName }, { it.eventClassName })).forEach {
        if (dump.unarySignificance[it] != 0.0) {
            val mVal = "%.4f".format(dump.unarySignificance[it])
            println("S_${it.metricName}(${it.eventClassName}) = $mVal")
        }
    }
    dump.binarySignificance.keys.sortedWith(compareBy({ it.metricName }, { it.eventClass1Name }, { it.eventClass2Name })).forEach {
        if (dump.binarySignificance[it] != 0.0) {
            val mVal = "%.4f".format(dump.binarySignificance[it])
            println("S_${it.metricName}(${it.eventClass1Name}, ${it.eventClass2Name}) = $mVal")
        }
    }
    dump.binaryCorrelation.keys.sortedWith(compareBy({ it.metricName }, { it.eventClass1Name }, { it.eventClass2Name })).forEach {
        if (dump.binaryCorrelation[it] != 0.0) {
            val mVal = "%.4f".format(dump.binaryCorrelation[it])
            println("C_${it.metricName}(${it.eventClass1Name}, ${it.eventClass2Name}) = $mVal")
        }
    }
    println()
}