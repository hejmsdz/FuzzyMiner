package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsStore
import org.deckfour.xes.classification.XEventClass

class GraphBuilder {
    fun buildFromMetrics(metrics: MetricsStore): Graph {
        val nodes = getNodes(metrics)
        val edges = getEdges(nodes, metrics)
        return Graph(nodes.values.toList(), edges)
    }

    private fun getNodes(metrics: MetricsStore): Map<XEventClass, Node> {
        return metrics.aggregateUnarySignificance.mapValues { (eventClass, significance) ->
            PrimitiveNode(eventClass, significance)
        }
    }

    private fun getEdges(nodes: Map<XEventClass, Node>, metrics: MetricsStore): Map<Node, List<Edge>> {
        val edges = mutableMapOf<Node, MutableList<Edge>>()
        metrics.aggregateBinarySignificance.forEach { (eventClasses, significance) ->
            val sourceNode = nodes.getValue(eventClasses.first)
            val targetNode = nodes.getValue(eventClasses.second)
            val edgesForNode = edges.getOrPut(sourceNode, { mutableListOf() })
            val correlation = metrics.aggregateBinaryCorrelation.getOrDefault(eventClasses, 0.0)
            if (significance > 0.0001 && correlation > 0.0001) {
                val edge = Edge(sourceNode, targetNode, significance, correlation)
                edgesForNode.add(edge)
            }
        }
        return edges
    }
}