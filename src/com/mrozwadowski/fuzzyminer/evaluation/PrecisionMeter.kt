package com.mrozwadowski.fuzzyminer.evaluation

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import org.deckfour.xes.classification.XEventClassifier
import org.deckfour.xes.model.XLog

class PrecisionMeter(private val graph: Graph, private val classifier: XEventClassifier) {
    fun calculate(log: XLog) = calculate(classifyLog(log, classifier))

    fun calculate(log: ClassifiedLog): Double {
        val eventPrecisions = log.flatMap { trace ->
            (1 until trace.size).map { prefixLength ->
                val prefix = trace.subList(0, prefixLength)
                val graphSuccessors = successorsFromGraph(prefix)
                if (graphSuccessors.isEmpty()) return@map 1.0
                val logSuccessors = successorsFromLog(prefix, log)
                (logSuccessors intersect graphSuccessors).size.toDouble() / graphSuccessors.size.toDouble()
            }
        }
        return if (eventPrecisions.isEmpty()) 0.0 else eventPrecisions.average()
    }

    fun successorsFromLog(prefix: ClassifiedTrace, classifiedLog: ClassifiedLog): Set<String> {
        return classifiedLog.filter { trace -> trace.size > prefix.size && trace.subList(0, prefix.size) == prefix}
            .map { trace -> trace[prefix.size] }
            .toSet()
    }

    fun successorsFromGraph(prefix: ClassifiedTrace): Set<String> {
        val eventClass = prefix.last()
        val node = findNode(graph, eventClass) ?: return emptySet()
        return graph.edgesFrom(node).flatMap { edge ->
            when (val nextNode = edge.target) {
                is PrimitiveNode -> listOf(nextNode.eventClass.id)
                is NodeCluster -> nextNode.nodes.map { it.eventClass.id }
                else -> listOf()
            }
        }.toSet()
    }
}
