package com.mrozwadowski.fuzzyminer.experiments

import com.mrozwadowski.fuzzyminer.input.getLogReader
import com.mrozwadowski.fuzzyminer.mining.FuzzyMiner
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsStore
import com.mrozwadowski.fuzzyminer.mining.metrics.defaultMetrics
import com.mrozwadowski.fuzzyminer.mining.online.OnlineFuzzyMiner
import com.mrozwadowski.fuzzyminer.output.Dot
import org.deckfour.xes.classification.XEventNameClassifier
import java.io.File
import kotlin.math.absoluteValue

fun main() {
    val log = getLogReader(File("sampleData/journal_review.xes")).readLog()
    val window = SlidingWindow(log, windowSize = 20, stride = 5)

    val classifier = XEventNameClassifier()
    val metrics = defaultMetrics()
    val onlineMiner = OnlineFuzzyMiner(classifier, metrics)

    onlineMiner.learn(window.initial())
    window.steps().forEach { step ->
        onlineMiner.learn(window.incoming(step))
        onlineMiner.unlearn(window.outgoing(step))
    }
    println(Dot(onlineMiner.graph))

    val lastFragment = window.fragment(window.steps().last)
    val offlineMetrics = defaultMetrics()
    val offlineMiner = FuzzyMiner(lastFragment, classifier, offlineMetrics)
    val offlineGraph = offlineMiner.mine()
    println(Dot(offlineGraph))

    compareMetrics(metrics, offlineMetrics)
}

fun compareMetrics(metrics1: MetricsStore, metrics2: MetricsStore) {
    metrics1.aggregateUnarySignificance.forEach { (key, value) ->
        val offlineValue = metrics2.aggregateUnarySignificance[key] ?: 0.0
        if ((offlineValue - value).absoluteValue > 0.0001) {
            println("US($key):\n    online[$value], offline[$offlineValue]")
        }
    }

    metrics1.aggregateBinarySignificance.forEach { (key, value) ->
        val offlineValue = metrics2.aggregateBinarySignificance[key] ?: 0.0
        if ((offlineValue - value).absoluteValue > 0.0001) {
            println("BS($key):\n    online[$value], offline[$offlineValue]")
        }
    }

    metrics1.aggregateBinaryCorrelation.forEach { (key, value) ->
        val offlineValue = metrics2.aggregateBinaryCorrelation[key] ?: 0.0
        if ((offlineValue - value).absoluteValue > 0.0001) {
            println("BC($key):\n    online[$value], offline[$offlineValue]")
        }
    }
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