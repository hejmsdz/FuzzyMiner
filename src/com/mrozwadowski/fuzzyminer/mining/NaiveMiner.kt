package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryFrequency
import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventClassifier
import org.deckfour.xes.model.XLog

class NaiveMiner(
    private val log: XLog,
    private val eventClasses: XEventClasses
) {
    constructor(log: XLog, classifier: XEventClassifier):
            this(log, XEventClasses.deriveEventClasses(classifier, log))

    fun mine(): Graph {
        val activitiesToNodes = getActivitiesToNodes()
        val nodes = activitiesToNodes.values.toList()
        val edges = getEdges(activitiesToNodes)
        return Graph(nodes, edges)
    }

    private fun getActivitiesToNodes(): Map<XEventClass, Node> {
        return eventClasses.classes.associateWith { PrimitiveNode(it) }
    }

    private fun getEdges(activitiesToNodes: Map<XEventClass, Node>):
            Map<Node, List<Edge>> {
        val edges = mutableMapOf<Node, MutableList<Edge>>()
        BinaryFrequency(log, eventClasses).allPairs().forEach { (source, target) ->
            val sourceNode = activitiesToNodes.getValue(source)
            val targetNode = activitiesToNodes.getValue(target)
            val edgesForNode = edges.getOrPut(sourceNode, { mutableListOf() })
            edgesForNode.add(Edge(sourceNode, targetNode))
        }
        return edges
    }
}