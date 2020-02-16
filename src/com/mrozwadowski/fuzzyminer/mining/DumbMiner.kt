package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Log

class DumbMiner(private val log: Log) {
    fun mine(): Graph {
        val nodes = log.activities.associateWith { Node(it.name, it.id) }
        val edges = findSuccessions().keys
            .groupBy { it.first }
            .map { nodes[it.key]!! to it.value.map { Edge(nodes[it.second]!!) } }
            .toMap()
        return Graph(nodes.values.toList(), edges)
    }

    private fun findSuccessions(): Map<Pair<Activity, Activity>, Int> {
        val successions = HashMap<Pair<Activity, Activity>, Int>()
        log.traces.forEach { trace ->
            val length = trace.events.size
            for (i in 1 until length) {
                val pair = Pair(trace.events[i - 1].activity, trace.events[i].activity)
                successions.putIfAbsent(pair, 0)
                successions[pair]!!.inc()
            }
        }
        return successions
    }
}