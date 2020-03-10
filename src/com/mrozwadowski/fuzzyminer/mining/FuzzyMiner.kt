package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryFrequency
import com.mrozwadowski.fuzzyminer.mining.metrics.EndpointCorrelation
import com.mrozwadowski.fuzzyminer.mining.simplification.ConflictResolver
import com.mrozwadowski.fuzzyminer.mining.simplification.EdgeFilter

class FuzzyMiner<EventClass>(private val log: Log, private val classifier: Classifier<EventClass>) {
    private val binarySignificance = BinaryFrequency(log, classifier)
    private val binaryCorrelation = EndpointCorrelation(log, classifier)
    private val preserveThreshold = 0.2
    private val ratioThreshold = 0.05
    private val utilityRatio = 0.5
    private val edgeCutoff = 0.2

    fun mine(): Graph<EventClass> {
        var graph = DumbMiner(log, classifier).mine()
        val cr = ConflictResolver(graph, binarySignificance)
        graph = cr.resolveConflicts(preserveThreshold, ratioThreshold)
        val ef = EdgeFilter(graph, binarySignificance, binaryCorrelation)
        graph = ef.filterEdges(utilityRatio, edgeCutoff)
        return graph
    }
}