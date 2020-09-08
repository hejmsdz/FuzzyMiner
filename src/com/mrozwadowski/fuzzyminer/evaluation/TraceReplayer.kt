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

    fun replayLog(log: XLog) = replayLog(classifyLog(log, classifier))
    fun replayTrace(trace: XTrace) = replayTrace(classifyTrace(trace, classifier))

    fun replayLog(log: ClassifiedLog): Double {
        return log.sumByDouble { replayTrace(it) * it.size } / log.sumBy { it.size }
    }

    fun replayTrace(trace: ClassifiedTrace): Double {
        var currentNode: Node?
        val validSuccessors = mutableSetOf<Node>()
        val matches = arrayOfNulls<Char>(trace.size)

        var lastNode: Node? = null
        for ((i, event) in trace.withIndex()) {
            currentNode = findNode(graph, event)
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
        return 1.0 - deviations.toDouble() / trace.size
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
}