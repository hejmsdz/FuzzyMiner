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
    companion object {
        const val VALID = '.'
        const val UNKNOWN = '?'
        const val INVALID_START = '^'
        const val INVALID_TRANSITION = '!'
        const val INVALID_END = '$'
    }

    fun replayLog(log: XLog): Double {
        return log.sumByDouble { replayTrace(it) } / log.size.toDouble()
    }

    fun replayTrace(trace: XTrace): Double {
        var currentNode: Node?
        val validSuccessors = mutableSetOf<Node>()
        val matches = arrayOfNulls<Char>(trace.size)

        var lastNode: Node? = null
        for ((i, event) in trace.withIndex()) {
            currentNode = findNodeForEvent(event)
            if (currentNode == null) {
                matches[i] = UNKNOWN
                continue
            }

            if (lastNode == null) {
                matches[i] = if (isValidStart(currentNode)) VALID else INVALID_START
            } else if (currentNode in validSuccessors) {
                validSuccessors.remove(currentNode)
                matches[i] = VALID
            } else {
                matches[i] = INVALID_TRANSITION
            }
            validSuccessors.addAll(graph.edgesFrom(currentNode).map { it.target })
            lastNode = currentNode
        }

        if (!isValidEnd(lastNode)) {
            matches[matches.size - 1] = INVALID_END
        }

        val deviations = matches.count { it != VALID }
        return (trace.size - deviations + 1).toDouble() / (trace.size + 1)
    }

    private fun isValidStart(node: Node?): Boolean {
        if (node == null) {
            return false
        }
        val predecessors = graph.edgesTo(node)
        val hasSelfLoopOnly = predecessors.size == 1 && predecessors.all { it.source == node }
        return predecessors.isEmpty() || hasSelfLoopOnly
    }

    private fun isValidEnd(node: Node?): Boolean {
        if (node == null) {
            return false
        }
        val successors = graph.edgesFrom(node)
        val hasSelfLoopOnly = successors.size == 1 && successors.all { it.source == node }
        return successors.isEmpty() || hasSelfLoopOnly
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