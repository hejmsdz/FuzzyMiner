package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsStore
import com.mrozwadowski.fuzzyminer.utils.significantlyGreater
import org.deckfour.xes.classification.XEventClass

class GraphBuilder {
    fun buildFromMetrics(metrics: MetricsStore): Graph {
        val nodes = getNodes(metrics)
        val edges = getEdges(nodes, metrics)
        return Graph(nodes.values.toList(), edges)
    }

    private fun getNodes(metrics: MetricsStore): Map<XEventClass, Node> {
        return metrics.aggregateUnarySignificance
            .filterValues { significantlyGreater(it, 0.0) }
            .mapValues { (eventClass, significance) ->
                PrimitiveNode(eventClass, significance)
            }
    }

    private fun getEdges(nodes: Map<XEventClass, Node>, metrics: MetricsStore): Map<Node, List<Edge>> {
        val edges = mutableMapOf<Node, MutableList<Edge>>()
        metrics.aggregateBinarySignificance.forEach { (eventClasses, significance) ->
            val sourceNode = nodes[eventClasses.first] ?: return@forEach
            val targetNode = nodes[eventClasses.second] ?: return@forEach
            val edgesForNode = edges.getOrPut(sourceNode, { mutableListOf() })
            val correlation = metrics.aggregateBinaryCorrelation.getOrDefault(eventClasses, 0.0)
            if (significantlyGreater(significance, 0.0) && significantlyGreater(correlation, 0.0)) {
                val edge = Edge(sourceNode, targetNode, significance, correlation)
                edgesForNode.add(edge)
            }
        }
        return edges
    }
}