package com.mrozwadowski.fuzzyminer.evaluation

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import org.deckfour.xes.classification.XEventClassifier
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace

class TraceReplayer(private val graph: Graph, private val classifier: XEventClassifier) {
    fun replayLog(log: XLog): Double {
        return log.count { replayTrace(it) } / log.size.toDouble()
    }

    fun replayTrace(trace: XTrace): Boolean {
        var lastNode: Node? = null
        for (event in trace) {
            val currentNode = findNodeForEvent(event)
            if (currentNode == null) {
                println("Could not find a node corresponding to $event")
                return false
            }
            if (lastNode == null) {
                if (!isValidStart(currentNode)) {
                    return false
                }
                lastNode = currentNode
                continue
            }
            graph.edgeBetween(lastNode, currentNode) ?: return false
            lastNode = currentNode
        }
        return isValidEnd(lastNode)
    }

    private fun isValidStart(node: Node?): Boolean {
        return node != null && graph.edgesTo(node).isEmpty()
    }

    private fun isValidEnd(node: Node?): Boolean {
        return node != null && graph.edgesFrom(node).isEmpty()
    }

    private fun findNodeForEvent(event: XEvent): Node? {
        val eventClass = classifier.getClassIdentity(event)
        return graph.nodes.find { node ->
            when (node) {
                is PrimitiveNode ->
                    node.eventClass.toString() == eventClass
                is NodeCluster ->
                    node.nodes.any { it.eventClass.toString() == eventClass }
                else -> false
            }
        }
    }
}