package com.mrozwadowski.fuzzyminer.experiments

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.input.getLogReader
import com.mrozwadowski.fuzzyminer.mining.FuzzyMiner
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsStore
import com.mrozwadowski.fuzzyminer.mining.metrics.defaultMetrics
import com.mrozwadowski.fuzzyminer.mining.online.OnlineFuzzyMiner
import org.deckfour.xes.classification.XEventNameClassifier
import java.io.File
import kotlin.math.absoluteValue

fun main() {
    val logPath = "sampleData/journal_review.xes"

    (10..50 step 10).forEach { windowSize ->
        listOf(windowSize / 5, windowSize / 2, 3 * windowSize / 5).forEach { stride ->
            println("$windowSize : $stride")
            test(logPath, windowSize, 5)
        }
    }
}

fun test(logPath: String, windowSize: Int, stride: Int) {
    val log = getLogReader(File(logPath)).readLog()
    val window = SlidingWindow(log, windowSize, stride)

    val classifier = XEventNameClassifier()
    val onlineMetrics = metricsFactory()
    val onlineMiner = OnlineFuzzyMiner(classifier, onlineMetrics)

    onlineMiner.learn(window.initial())
    window.steps().forEach { step ->
        onlineMiner.learn(window.incoming(step))
        onlineMiner.unlearn(window.outgoing(step))
        val onlineGraph = onlineMiner.graph

        val fragment = window.fragment(step)
        val offlineMetrics = metricsFactory()
        val offlineMiner = FuzzyMiner(fragment, classifier, offlineMetrics)
        val offlineGraph = offlineMiner.mine()

        compareGraphs(onlineGraph, offlineGraph)
        compareMetrics(onlineMetrics, offlineMetrics)
    }
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

fun compareGraphs(onlineGraph: Graph, offlineGraph: Graph, verbose: Boolean = false) {
    var difference = 0.0

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
            difference += node2?.significance ?: 0.0
            if (verbose) println("$nodeName missing in online graph")
            return@forEach
        }
        if (node2 == null) {
            difference += node1.significance
            if (verbose) println("$nodeName excessive in online graph")
            return@forEach
        }
        if ((node1.significance - node2.significance).absoluteValue > 0.0001) {
            difference += (node1.significance - node2.significance)
            if (verbose) println("$nodeName has significance ${node1.significance} in online graph and ${node2.significance} in offline graph")
        }
    }

    allEdgeNames.forEach { edgeName ->
        val edge1 = edges1[edgeName]
        val edge2 = edges2[edgeName]
        if (edge1 == null) {
            difference += (edge2?.significance ?: 0.0) + (edge2?.correlation ?: 0.0)
            if (verbose) println("$edgeName [sig = ${edge2?.significance}, cor = ${edge2?.correlation}] missing in online graph")
            return@forEach
        }
        if (edge2 == null) {
            difference += edge1.significance + edge1.correlation
            if (verbose) println("$edgeName [sig = ${edge1.significance}, cor = ${edge1.correlation}] excessive in online graph")
            return@forEach
        }
        if ((edge1.significance - edge2.significance).absoluteValue > 0.0001) {
            difference += (edge1.significance - edge2.significance)
            if (verbose) println("$edgeName has significance ${edge1.significance} in online graph and ${edge2.significance} in offline graph")
        }
        if ((edge1.correlation - edge2.correlation).absoluteValue > 0.0001) {
            difference += (edge1.correlation - edge2.correlation)
            if (verbose) println("$edgeName has correlation ${edge1.correlation} in online graph and ${edge2.correlation} in offline graph")
        }
    }

    if (difference > 0.0) {
        println("Total difference: $difference")
    } else if (verbose) {
        println("Perfect match!")
    }
}
