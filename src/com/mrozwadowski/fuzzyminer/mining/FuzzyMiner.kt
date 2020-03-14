package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryFrequency
import com.mrozwadowski.fuzzyminer.mining.metrics.EndpointCorrelation
import com.mrozwadowski.fuzzyminer.mining.simplification.ConcurrencyFilter
import com.mrozwadowski.fuzzyminer.mining.simplification.EdgeFilter
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventClassifier
import org.deckfour.xes.model.XLog

class FuzzyMiner(
    private val log: XLog,
    private val eventClasses: XEventClasses
) {
    constructor(log: XLog, classifier: XEventClassifier):
            this(log, XEventClasses.deriveEventClasses(classifier, log))

    private val binarySignificance = BinaryFrequency(log, eventClasses)
    private val binaryCorrelation = EndpointCorrelation()
    private val preserveThreshold = 0.2
    private val ratioThreshold = 0.05
    private val utilityRatio = 0.5
    private val edgeCutoff = 0.2

    fun mine(): Graph {
        var graph = NaiveMiner(log, eventClasses).mine()
        graph = filterConcurrency(graph)
        graph = filterEdges(graph)
        return graph
    }

    fun filterConcurrency(graph: Graph): Graph {
        val filter = ConcurrencyFilter(graph, binarySignificance)
        return filter.apply(preserveThreshold, ratioThreshold)
    }

    fun filterEdges(graph: Graph): Graph {
        val filter = EdgeFilter(graph, binarySignificance, binaryCorrelation)
        return filter.apply(utilityRatio, edgeCutoff)
    }
}