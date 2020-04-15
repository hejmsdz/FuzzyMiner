package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryFrequency
import com.mrozwadowski.fuzzyminer.mining.metrics.EndpointCorrelation
import com.mrozwadowski.fuzzyminer.mining.metrics.UnaryFrequency
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.EdgeMetric
import com.mrozwadowski.fuzzyminer.mining.metrics.graph.NodeMetric
import com.mrozwadowski.fuzzyminer.mining.simplification.ClusterMerger
import com.mrozwadowski.fuzzyminer.mining.simplification.ConcurrencyFilter
import com.mrozwadowski.fuzzyminer.mining.simplification.EdgeFilter
import com.mrozwadowski.fuzzyminer.mining.simplification.VictimClusterer
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventClassifier
import org.deckfour.xes.model.XLog

class FuzzyMiner(
    private val log: XLog,
    private val eventClasses: XEventClasses
) {
    constructor(log: XLog, classifier: XEventClassifier):
            this(log, XEventClasses.deriveEventClasses(classifier, log))

    private val unarySignificance = NodeMetric(UnaryFrequency(log, eventClasses))
    private val binarySignificance = EdgeMetric(BinaryFrequency(log, eventClasses))
    private val binaryCorrelation = EdgeMetric(EndpointCorrelation())
    private val preserveThreshold = 0.2
    private val ratioThreshold = 0.05
    private val utilityRatio = 0.5
    private val edgeCutoff = 0.2
    private val nodeCutoff = 0.1

    fun mine(): Graph {
        var graph = NaiveMiner(log, eventClasses).mine()
        graph = filterConcurrency(graph)
        graph = filterEdges(graph)
        graph = filterNodes(graph)
        return graph
    }

    private fun filterConcurrency(graph: Graph): Graph {
        val filter = ConcurrencyFilter(graph, binarySignificance)
        return filter.apply(preserveThreshold, ratioThreshold)
    }

    private fun filterEdges(graph: Graph): Graph {
        val filter = EdgeFilter(graph, binarySignificance, binaryCorrelation)
        return filter.apply(utilityRatio, edgeCutoff)
    }

    private fun filterNodes(graph: Graph): Graph {
        val filter1 = VictimClusterer(graph, unarySignificance, binaryCorrelation)
        val graph1 = filter1.apply(nodeCutoff)
        val filter2 = ClusterMerger(graph1, binaryCorrelation)
        return filter2.apply()
    }
}