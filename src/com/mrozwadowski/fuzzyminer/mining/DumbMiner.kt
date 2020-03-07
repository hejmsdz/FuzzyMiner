package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryFrequency

open class DumbMiner<EventClass>(
    protected val log: Log,
    private val classifier: Classifier<EventClass>
) {
    fun mine(): Graph {
        val activitiesToNodes = getActivitiesToNodes()
        val nodes = activitiesToNodes.values.toList()
        val edges = getEdges(activitiesToNodes)
        return Graph(nodes, edges)
    }

    private fun getActivitiesToNodes(): Map<EventClass, Node> {
        return log.eventClasses(classifier)
            .associateWith { eventClass -> Node(eventClass.toString(), 0) }
    }

    private fun getEdges(activitiesToNodes: Map<EventClass, Node>): Map<Node, List<Edge>> {
        val edges = mutableMapOf<Node, MutableList<Edge>>()
        BinaryFrequency(log, classifier).allPairs().forEach { (source, target) ->
            val sourceNode = activitiesToNodes.getValue(source)
            val targetNode = activitiesToNodes.getValue(target)
            val edgesForNode = edges.getOrPut(sourceNode, { mutableListOf() })
            edgesForNode.add(Edge(targetNode))
        }
        return edges
    }
}