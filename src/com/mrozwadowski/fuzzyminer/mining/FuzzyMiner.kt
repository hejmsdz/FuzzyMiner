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

    var parameters = Parameters(0.2, 0.05, 0.5 ,0.2, 0.1)

    fun mine(): Graph {
        var graph = NaiveMiner(log, eventClasses, metrics).mine()
        graph = filterConcurrency(graph)
        graph = filterEdges(graph)
        graph = filterNodes(graph)
        sanityCheck("cluster filtering", graph)
        return graph
    }

    private fun sanityCheck(stage: String, graph: Graph) {
        val edges = graph.allEdgeObjects()
        val significantEdges = edges.count { it.significance > 0 }
        val correlatedEdges = edges.count { it.significance > 0 }
        println("$stage: ${edges.size} total, $significantEdges significant, $correlatedEdges correlated")
    }

    private fun filterConcurrency(graph: Graph): Graph {
        val filter = ConcurrencyFilter(graph)
        return filter.apply(parameters.preserveThreshold, parameters.ratioThreshold)
    }

    private fun filterEdges(graph: Graph): Graph {
        val filter = EdgeFilter(graph)
        return filter.apply(parameters.utilityRatio, parameters.edgeCutoff)
    }

    private fun filterNodes(graph: Graph): Graph {
        val filter1 = VictimClusterer(graph)
        val graph1 = filter1.apply(parameters.nodeCutoff)
        sanityCheck("first clustering", graph1)
        val filter2 = ClusterMerger(graph1)
        val graph2 = filter2.apply()
        sanityCheck("cluster merging", graph2)
        val filter3 = ClusterFilter(graph2)
        return filter3.apply()
    }
}