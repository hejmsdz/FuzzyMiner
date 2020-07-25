package com.mrozwadowski.fuzzyminer.experiments

import com.mrozwadowski.fuzzyminer.data.graph.Graph
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
    val metrics = metricsFactory()
    val onlineMiner = OnlineFuzzyMiner(classifier, metrics)

    onlineMiner.learn(window.initial())
    window.steps().forEach { step ->
        onlineMiner.learn(window.incoming(step))
        onlineMiner.unlearn(window.outgoing(step))
    }
    println(Dot(onlineMiner.graph))

    val lastFragment = window.fragment(window.steps().last)
    val offlineMetrics = metricsFactory()
    val offlineMiner = FuzzyMiner(lastFragment, classifier, offlineMetrics)
    val offlineGraph = offlineMiner.mine()
    println(Dot(offlineGraph))

    compareMetrics(metrics, offlineMetrics)
    compareGraphs(onlineMiner.graph, offlineGraph)
}

fun metricsFactory() = defaultMetrics()

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

fun compareGraphs(onlineGraph: Graph, offlineGraph: Graph) {
    val nodes1 = onlineGraph.nodes.map { it.toString() to it }.toMap()
    val nodes2 = offlineGraph.nodes.map { it.toString() to it }.toMap()
    val edges1 = onlineGraph.allEdgeObjects().map { (it.source.toString() to it.target.toString()) to it }.toMap()
    val edges2 = offlineGraph.allEdgeObjects().map { (it.source.toString() to it.target.toString()) to it }.toMap()

    val allNodeNames = nodes1.keys + nodes2.keys
    val allEdgeNames = edges1.keys + edges2.keys

    allNodeNames.forEach { nodeName ->
        val node1 = nodes1[nodeName]
        val node2 = nodes2[nodeName]
        if (node1 == null) {
            println("$nodeName missing in online graph")
            return@forEach
        }
        if (node2 == null) {
            println("$nodeName excessive in online graph")
            return@forEach
        }
        if ((node1.significance - node2.significance).absoluteValue > 0.0001) {
            println("$nodeName has significance ${node1.significance} in online graph and ${node2.significance} in offline graph")
        }
    }

    allEdgeNames.forEach { edgeName ->
        val edge1 = edges1[edgeName]
        val edge2 = edges2[edgeName]
        if (edge1 == null) {
            println("$edgeName [sig = ${edge2?.significance}, cor = ${edge2?.correlation}] missing in online graph")
            return@forEach
        }
        if (edge2 == null) {
            println("$edgeName [sig = ${edge1.significance}, cor = ${edge1.correlation}] excessive in online graph")
            return@forEach
        }
        if ((edge1.significance - edge2.significance).absoluteValue > 0.0001) {
            println("$edgeName has significance ${edge1.significance} in online graph and ${edge2.significance} in offline graph")
        }
        if ((edge1.correlation - edge2.correlation).absoluteValue > 0.0001) {
            println("$edgeName has correlation ${edge1.correlation} in online graph and ${edge2.correlation} in offline graph")
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