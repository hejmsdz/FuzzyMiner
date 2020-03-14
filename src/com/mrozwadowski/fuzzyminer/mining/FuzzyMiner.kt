package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryFrequency
import com.mrozwadowski.fuzzyminer.mining.metrics.EndpointCorrelation
import com.mrozwadowski.fuzzyminer.mining.simplification.ConflictResolver
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
        var graph = DumbMiner(log, eventClasses).mine()
        val cr = ConflictResolver(graph, binarySignificance)
        graph = cr.resolveConflicts(preserveThreshold, ratioThreshold)
        val ef = EdgeFilter(graph, binarySignificance, binaryCorrelation)
        graph = ef.filterEdges(utilityRatio, edgeCutoff)
        return graph
    }
}