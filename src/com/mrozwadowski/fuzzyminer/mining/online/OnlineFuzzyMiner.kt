package com.mrozwadowski.fuzzyminer.mining.online

import com.mrozwadowski.fuzzyminer.data.Parameters
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.mining.GraphBuilder
import com.mrozwadowski.fuzzyminer.mining.SimplificationPipeline
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsDump
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsStore
import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventClassifier
import org.deckfour.xes.model.XLog
import java.io.File
import java.io.IOException

class XEventClassesExtended(classifier: XEventClassifier): XEventClasses(classifier) {
    fun register(eventClass: XEventClass) {
        classMap.putIfAbsent(eventClass.id, eventClass)
    }
}

class OnlineFuzzyMiner(
    private val classifier: XEventClassifier,
    private var metrics: MetricsStore
) {
    var parameters = Parameters(0.6, 0.7, 0.75 ,0.8, 0.1)

    val graph: Graph
        get() {
            if (isGraphStale) updateGraph()
            return savedGraph
        }

    private var savedGraph: Graph = Graph(listOf(), mapOf())
    private var isGraphStale = false

    fun learnUnlearn(incoming: XLog, outgoing: XLog) {
        metrics.reset()
        metrics.calculateFromBatches(listOf(
            Triple(incoming, getEventClasses(incoming), 1),
            Triple(outgoing, getEventClasses(outgoing), -1)
        ))
        isGraphStale = true
    }

    fun learn(log: XLog) {
        val eventClasses = getEventClasses(log)
        metrics.reset()
        metrics.calculateFromLog(log, eventClasses)
        isGraphStale = true
    }

    fun unlearn(log: XLog) {
        val eventClasses = getEventClasses(log)
        metrics.reset()
        metrics.calculateFromLog(log, eventClasses, -1)
        isGraphStale = true
    }

    fun load(file: File) {
        val json = file.readText()
        val dump = MetricsDump.fromString(json) ?: throw IOException("Failed to parse dumped model!")
        metrics.loadMetrics(dump)
        isGraphStale = true
    }

    fun save(file: File) {
        val dump = metrics.dumpMetrics()
        val json = dump.toString()
        file.writeText(json)
    }

    private fun updateGraph() {
        val originalGraph = GraphBuilder().buildFromMetrics(metrics)
        savedGraph = SimplificationPipeline(parameters).simplify(originalGraph)
        isGraphStale = false
    }

    fun getEventClasses(log: XLog): XEventClasses {
        val eventClasses = XEventClassesExtended(classifier)
        knownEventClasses().forEach { eventClasses.register(it) }
        eventClasses.register(log)
        return eventClasses
    }

    private fun knownEventClasses(): Collection<XEventClass> {
        return metrics.aggregateUnarySignificance.keys
    }
}
