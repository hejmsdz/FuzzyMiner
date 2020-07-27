package com.mrozwadowski.fuzzyminer.mining.online

import com.mrozwadowski.fuzzyminer.data.Parameters
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.mining.GraphBuilder
import com.mrozwadowski.fuzzyminer.mining.SimplificationPipeline
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsStore
import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventClassifier
import org.deckfour.xes.model.XLog

class XEventClassesExtended(classifier: XEventClassifier): XEventClasses(classifier) {
    fun register(eventClass: XEventClass) {
        classMap.putIfAbsent(eventClass.id, eventClass)
    }
}

class OnlineFuzzyMiner(
    private val classifier: XEventClassifier,
    private var metrics: MetricsStore
) {
    var parameters = Parameters(0.2, 0.05, 0.5 ,0.2, 0.1)

    val graph: Graph
        get() {
            if (isGraphStale) updateGraph()
            return savedGraph
        }

    private var savedGraph: Graph = Graph(listOf(), mapOf())
    private var isGraphStale = false

    fun learn(log: XLog) {
        val eventClasses = getEventClasses(log)
        metrics.calculateFromLog(log, eventClasses)
        isGraphStale = true
    }

    fun unlearn(log: XLog) {
        val eventClasses = getEventClasses(log)
        metrics.reset()
        metrics.calculateFromLog(log, eventClasses, -1.0)
        isGraphStale = true
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
