package com.mrozwadowski.fuzzyminer.mining.online

import com.mrozwadowski.fuzzyminer.data.Parameters
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.FuzzyMiner
import com.mrozwadowski.fuzzyminer.mining.GraphBuilder
import com.mrozwadowski.fuzzyminer.mining.SimplificationPipeline
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsDump
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

    var graph: Graph = Graph(listOf(), mapOf())

    fun learn(log: XLog) {
        val eventClasses = getEventClasses(log)
        metrics.calculateFromLog(log, eventClasses)
        updateGraph()
    }

    fun unlearn(log: XLog) {
        val eventClasses = getEventClasses(log)
        metrics.reset()
        metrics.calculateFromLog(log, eventClasses, -1.0)
        updateGraph()
    }

    private fun updateGraph() {
        val originalGraph = GraphBuilder().buildFromMetrics(metrics)
        graph = SimplificationPipeline(parameters).simplify(originalGraph)
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
