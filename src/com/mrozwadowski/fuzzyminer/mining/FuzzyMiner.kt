package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.Parameters
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.mining.metrics.MetricsStore
import com.mrozwadowski.fuzzyminer.mining.simplification.*
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.classification.XEventClassifier
import org.deckfour.xes.model.XLog

class FuzzyMiner(
    private val log: XLog,
    private val eventClasses: XEventClasses,
    private val metrics: MetricsStore
) {
    constructor(log: XLog, classifier: XEventClassifier, metrics: MetricsStore):
            this(log, XEventClasses.deriveEventClasses(classifier, log), metrics)

    var parameters = Parameters(0.6, 0.7, 0.75 ,0.8, 0.1)

    fun mine(): Graph {
        metrics.calculateFromLog(log, eventClasses)
        val graph = GraphBuilder().buildFromMetrics(metrics)
        return SimplificationPipeline(parameters).simplify(graph)
    }
}