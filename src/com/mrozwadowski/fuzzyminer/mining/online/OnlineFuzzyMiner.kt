package com.mrozwadowski.fuzzyminer.mining.online

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.FuzzyMiner
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
    private val metrics: MetricsStore,
    var graph: Graph? = null
) {
    fun learn(log: XLog) {
        if (graph == null) {
            graph = FuzzyMiner(log, classifier, metrics).mine()
        }

        val eventClasses = getEventClasses(log)
    }

    fun unlearn(log: XLog) {
        if (graph == null) {
            return
        }
    }

    fun getEventClasses(log: XLog): XEventClasses {
        val eventClasses = XEventClassesExtended(classifier)
        knownEventClasses().forEach { eventClasses.register(it) }
        eventClasses.register(log)
        return eventClasses
    }

    private fun knownEventClasses(): List<XEventClass> {
        return graph?.nodes?.flatMap { node ->
            when (node) {
                is PrimitiveNode -> listOf(node.eventClass)
                is NodeCluster -> node.nodes.map { it.eventClass }
                else -> listOf()
            }
        } ?: listOf()
    }
}