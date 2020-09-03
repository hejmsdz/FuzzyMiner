package com.mrozwadowski.fuzzyminer.timingExperiment

import com.mrozwadowski.fuzzyminer.experiments.SlidingWindow

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.evaluation.TraceReplayer
import com.mrozwadowski.fuzzyminer.mining.FuzzyMiner
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsStore
import com.mrozwadowski.fuzzyminer.mining.metrics.defaultMetrics
import com.mrozwadowski.fuzzyminer.mining.online.OnlineFuzzyMiner
import com.mrozwadowski.fuzzyminer.utils.significantlyEqual
import org.deckfour.xes.classification.XEventNameClassifier
import org.deckfour.xes.model.XLog

fun main() {
    val logFiles = getLogFiles()
    val dao = FitnessCsvDao("./conformance.csv")
    try {
        experimentLoop(logFiles) { log, windowSize, stride, logName ->
            ConformanceComparison(dao, log, logName, windowSize, stride).testGraphIdentity()
        } || return
    } finally {
        dao.close()
    }
}

class FitnessCsvDao(path: String): CsvDao(listOf("identical", "fitness"), path)

class ConformanceComparison(private val dao: CsvDao?, log: XLog, private val logName: String, private val windowSize: Int, private val stride: Int) {
    val window = SlidingWindow(log, windowSize, stride)
    val classifier = XEventNameClassifier()

    fun testGraphIdentity() {
        val onlineMetrics = metricsFactory()
        val onlineMiner = OnlineFuzzyMiner(classifier, onlineMetrics)

        onlineMiner.learn(window.initial())
        window.steps().forEach { step ->
            onlineMiner.learnUnlearn(window.incoming(step), window.outgoing(step))
            val onlineGraph = onlineMiner.graph

            val fragment = window.fragment(step)
            val offlineMetrics = metricsFactory()
            val offlineMiner = FuzzyMiner(fragment, classifier, offlineMetrics)
            val offlineGraph = offlineMiner.mine()

            val identical = if (compareGraphs(onlineGraph, offlineGraph, verbose = true)) 1 else 0

            val replayer = TraceReplayer(onlineGraph, classifier)
            val fitness = replayer.replayLog(fragment)

            reportResults(step, identical, fitness)
        }
    }

    private fun reportResults(step: Int, identical: Int, fitness: Double) {
        dao?.insert(logName, windowSize, stride, step, listOf(identical, fitness))
    }
}

fun compareMetrics(metrics1: MetricsStore, metrics2: MetricsStore) {
    metrics1.aggregateUnarySignificance.forEach { (key, value) ->
        val offlineValue = metrics2.aggregateUnarySignificance[key] ?: 0.0
        if (!significantlyEqual(offlineValue, value)) {
            println("US($key):\n    online[$value], offline[$offlineValue]")
        }
    }

    metrics1.aggregateBinarySignificance.forEach { (key, value) ->
        val offlineValue = metrics2.aggregateBinarySignificance[key] ?: 0.0
        if (!significantlyEqual(offlineValue, value)) {
            println("BS($key):\n    online[$value], offline[$offlineValue]")
        }
    }

    metrics1.aggregateBinaryCorrelation.forEach { (key, value) ->
        val offlineValue = metrics2.aggregateBinaryCorrelation[key] ?: 0.0
        if (!significantlyEqual(offlineValue, value)) {
            println("BC($key):\n    online[$value], offline[$offlineValue]")
        }
    }
}

fun compareGraphs(onlineGraph: Graph, offlineGraph: Graph, verbose: Boolean = false): Boolean {
    var nodeDiff = 0
    var edgeDiff = 0
    var metricDiff = 0.0

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
            nodeDiff++
            if (verbose) println("$nodeName missing in online graph")
            return@forEach
        }
        if (node2 == null) {
            nodeDiff++
            if (verbose) println("$nodeName excessive in online graph")
            return@forEach
        }
        if (!significantlyEqual(node1.significance, node2.significance)) {
            metricDiff += (node1.significance - node2.significance)
            if (verbose) println("$nodeName has significance ${node1.significance} in online graph and ${node2.significance} in offline graph")
        }
    }

    allEdgeNames.forEach { edgeName ->
        val edge1 = edges1[edgeName]
        val edge2 = edges2[edgeName]
        if (edge1 == null) {
            edgeDiff++
            if (verbose) println("$edgeName [sig = ${edge2?.significance}, cor = ${edge2?.correlation}] missing in online graph")
            return@forEach
        }
        if (edge2 == null) {
            edgeDiff++
            if (verbose) println("$edgeName [sig = ${edge1.significance}, cor = ${edge1.correlation}] excessive in online graph")
            return@forEach
        }
        if (!significantlyEqual(edge1.significance, edge2.significance)) {
            metricDiff += (edge1.significance - edge2.significance)
            if (verbose) println("$edgeName has significance ${edge1.significance} in online graph and ${edge2.significance} in offline graph")
        }
        if (!significantlyEqual(edge1.correlation, edge2.correlation)) {
            metricDiff += (edge1.correlation - edge2.correlation)
            if (verbose) println("$edgeName has correlation ${edge1.correlation} in online graph and ${edge2.correlation} in offline graph")
        }
    }

    return nodeDiff == 0 && edgeDiff == 0 && metricDiff == 0.0
}
