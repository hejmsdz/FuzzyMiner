package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryFrequency

open class DumbMiner(protected val log: Log) {
    fun mine(): Graph {
        val activitiesToNodes = getActivitiesToNodes()
        val nodes = activitiesToNodes.values.toList()
        val edges = getEdges(activitiesToNodes)
        return Graph(nodes, edges)
    }

    private fun getActivitiesToNodes(): Map<Activity, Node> {
        return log.activities.associateWith { Node(it.name, it.id) }
    }

    private fun getEdges(activitiesToNodes: Map<Activity, Node>): Map<Node, List<Edge>> {
        val edges = mutableMapOf<Node, MutableList<Edge>>()
        BinaryFrequency(log).allPairs().forEach { (source, target) ->
            val sourceNode = activitiesToNodes.getValue(source)
            val targetNode = activitiesToNodes.getValue(target)
            val edgesForNode = edges.getOrPut(sourceNode, { mutableListOf() })
            edgesForNode.add(Edge(targetNode))
        }
        return edges
    }
}