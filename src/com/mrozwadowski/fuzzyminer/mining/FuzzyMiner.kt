package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.classifiers.Classifier
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryFrequency
import com.mrozwadowski.fuzzyminer.mining.simplification.ConflictResolver

class FuzzyMiner<EventClass>(private val log: Log, private val classifier: Classifier<EventClass>) {
    private val binarySignificance = BinaryFrequency(log, classifier)
    private val preserveThreshold = 0.5
    private val ratioThreshold = 0.5

    fun mine(): Graph<EventClass> {
        val graph = DumbMiner(log, classifier).mine()
        val cr = ConflictResolver(graph, binarySignificance)
        cr.resolveConflicts(preserveThreshold, ratioThreshold)
        return graph
    }
}