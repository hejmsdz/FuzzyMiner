package com.mrozwadowski.fuzzyminer.evaluation

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import org.deckfour.xes.classification.XEventClassifier
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace

typealias ClassifiedTrace = List<String>
typealias ClassifiedLog = List<ClassifiedTrace>

fun classifyLog(log: XLog, classifier: XEventClassifier): ClassifiedLog {
    return log.map { classifyTrace(it, classifier) }
}

fun classifyTrace(trace: XTrace, classifier: XEventClassifier): ClassifiedTrace {
    return trace.map { event -> classifier.getClassIdentity(event) }
}

fun findNode(graph: Graph, eventClass: String): Node? {
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