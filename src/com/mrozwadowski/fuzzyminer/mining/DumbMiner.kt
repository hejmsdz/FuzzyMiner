package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryFrequency

open class DumbMiner<EventClass>(
    protected val log: Log,
    private val classifier: Classifier<EventClass>
) {
    fun mine(): Graph<EventClass> {
        val activitiesToNodes = getActivitiesToNodes()
        val nodes = activitiesToNodes.values.toList()
        val edges = getEdges(activitiesToNodes)
        return Graph(nodes, edges)
    }

    private fun getActivitiesToNodes(): Map<EventClass, Node<EventClass>> {
        return log.eventClasses(classifier)
            .withIndex()
            .associate { (index, eventClass) -> eventClass to Node(eventClass, index) }
    }

    private fun getEdges(activitiesToNodes: Map<EventClass, Node<EventClass>>):
            Map<Node<EventClass>, List<Edge<EventClass>>> {
        val edges = mutableMapOf<Node<EventClass>, MutableList<Edge<EventClass>>>()
        BinaryFrequency(log, classifier).allPairs().forEach { (source, target) ->
            val sourceNode = activitiesToNodes.getValue(source)
            val targetNode = activitiesToNodes.getValue(target)
            val edgesForNode = edges.getOrPut(sourceNode, { mutableListOf() })
            edgesForNode.add(Edge(sourceNode, targetNode))
        }
        return edges
    }
}