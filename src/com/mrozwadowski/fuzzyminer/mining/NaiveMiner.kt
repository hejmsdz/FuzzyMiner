package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsStore
import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventClassifier
import org.deckfour.xes.model.XLog


class NaiveMiner(
    private val log: XLog,
    private val eventClasses: XEventClasses,
    private val metrics: MetricsStore
) {
    constructor(log: XLog, classifier: XEventClassifier, metrics: MetricsStore):
            this(log, XEventClasses.deriveEventClasses(classifier, log), metrics)

    fun mine(): Graph {
        metrics.calculateFromLog(log, eventClasses)

        val nodes = getNodes()
        val edges = getEdges(nodes)
        return Graph(nodes.values.toList(), edges)
    }

    private fun getNodes(): Map<XEventClass, Node> {
        return metrics.aggregateUnarySignificance.mapValues { (eventClass, significance) ->
            PrimitiveNode(eventClass, significance)
        }
    }

    private fun getEdges(nodes: Map<XEventClass, Node>): Map<Node, List<Edge>> {
        val edges = mutableMapOf<Node, MutableList<Edge>>()
        metrics.aggregateBinarySignificance.forEach { (eventClasses, significance) ->
            val sourceNode = nodes.getValue(eventClasses.first)
            val targetNode = nodes.getValue(eventClasses.second)
            val edgesForNode = edges.getOrPut(sourceNode, { mutableListOf() })
            val correlation = metrics.aggregateBinaryCorrelation.getOrDefault(eventClasses, 0.0)
            val edge = Edge(sourceNode, targetNode, significance, correlation)
            edgesForNode.add(edge)
        }
        return edges
    }
}