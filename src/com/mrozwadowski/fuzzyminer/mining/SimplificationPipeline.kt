package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.Parameters
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.mining.simplification.*

class SimplificationPipeline(private val parameters: Parameters) {
    fun simplify(originalGraph: Graph): Graph {
        var graph = originalGraph
        graph = filterConcurrency(graph)
        graph = filterEdges(graph)
        graph = filterNodes(graph)
        return graph
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
        val filter2 = ClusterMerger(graph1)
        val graph2 = filter2.apply()
        val filter3 = ClusterFilter(graph2)
        return filter3.apply()
    }
}