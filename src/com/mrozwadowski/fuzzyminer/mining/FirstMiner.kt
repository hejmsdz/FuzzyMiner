package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryFrequency
import com.mrozwadowski.fuzzyminer.mining.metrics.updated.DerivedUnaryMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.updated.LogBasedBinaryMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.updated.LogBasedUnaryMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.updated.MetricsStore
import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventClassifier
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog
import kotlin.math.absoluteValue

class UnFreq: LogBasedUnaryMetric() {
    override fun evaluate(event: XEvent) = 1.0
}

class RoutingSignificance: DerivedUnaryMetric() {
    override fun calculate(
        unarySignificance: Map<XEventClass, Double>,
        binarySignificance: Map<Pair<XEventClass, XEventClass>, Double>,
        binaryCorrelation: Map<Pair<XEventClass, XEventClass>, Double>
    ) {
        val nodes = unarySignificance.keys
        nodes.forEach { node1 ->
            var inValue = 0.0
            var outValue = 0.0
            nodes.filter { it != node1 }.forEach { node2 ->
                inValue += binarySignificance.getOrDefault(node2 to node1, 0.0)
                outValue += binarySignificance.getOrDefault(node1 to node2, 0.0)
            }
            if (inValue > 0.0 && outValue > 0.0) {
                values[node1] = ((inValue - outValue) / (inValue + outValue)).absoluteValue
            }
        }
    }
}

class BinFreq: LogBasedBinaryMetric() {
    override fun evaluate(previousEvent: XEvent, event: XEvent) = 1.0
}

class FirstMiner(
    private val log: XLog,
    private val eventClasses: XEventClasses
) {
    constructor(log: XLog, classifier: XEventClassifier):
            this(log, XEventClasses.deriveEventClasses(classifier, log))

    private val metricStore = MetricsStore(mapOf(
        UnFreq() to 0.5,
        RoutingSignificance() to 0.5
    ), mapOf(
        BinFreq() to 0.5
    ), mapOf())

    fun mine(): Graph {
        processLog()

        val nodes = getNodes()
        val edges = getEdges(nodes)
        return Graph(nodes.values.toList(), edges)
    }

    private fun processLog() {
        log.forEach { trace ->
            trace.withIndex().forEach { (i, event) ->
                val eventClass = eventClasses.getClassOf(event)
                metricStore.processEvent(event, eventClass)

                if (i >= 1) {
                    val previousEvent = trace[i - 1]
                    val previousEventClass = eventClasses.getClassOf(previousEvent)
                    metricStore.processRelation(previousEvent, previousEventClass, event, eventClass, 1.0)
                }
            }
        }
        metricStore.calculateDerivedMetrics()
    }

    private fun getNodes(): Map<XEventClass, Node> {
        return metricStore.aggregateUnarySignificance.mapValues { (eventClass, significance) ->
            val node = PrimitiveNode(eventClass)
            node.significance = significance
            node
        }
    }

    private fun getEdges(nodes: Map<XEventClass, Node>): Map<Node, List<Edge>> {
        val edges = mutableMapOf<Node, MutableList<Edge>>()
        metricStore.aggregateBinarySignificance.forEach { (eventClasses, significance) ->
            val sourceNode = nodes.getValue(eventClasses.first)
            val targetNode = nodes.getValue(eventClasses.second)
            val edgesForNode = edges.getOrPut(sourceNode, { mutableListOf() })
            val edge = Edge(sourceNode, targetNode)
            edge.significance = significance
            edge.correlation = metricStore.aggregateBinaryCorrelation.getOrDefault(eventClasses)
            edgesForNode.add(edge)
        }
        return edges
    }
}