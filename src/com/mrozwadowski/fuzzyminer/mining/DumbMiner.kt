package com.mrozwadowski.fuzzyminer.mining

import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Log

class DumbMiner(private val log: Log) {
    fun mine(): Graph {
        val activitiesToNodes = getActivitiesToNodes()
        val nodes = activitiesToNodes.values.toList()
        val edges = getEdges(activitiesToNodes)
        return Graph(nodes, edges)
    }

    private fun getActivitiesToNodes(): Map<Activity, Node> {
        return log.activities.associateWith { Node(it.name, it.id) }
    }

    private fun getEdges(activitiesToNodes: Map<Activity, Node>): Map<Node, List<Edge>> {
        val edges = mutableMapOf<Node, MutableList<Edge>>()
        findSuccessions().forEach { (activities, _) ->
            val sourceNode = activitiesToNodes.getValue(activities.first)
            val targetNode = activitiesToNodes.getValue(activities.second)
            val edgesForNode = edges.getOrPut(sourceNode, { mutableListOf() })
            edgesForNode.add(Edge(targetNode))
        }
        return edges
    }

    private fun findSuccessions(): Map<Pair<Activity, Activity>, Int> {
        val successions = HashMap<Pair<Activity, Activity>, Int>()
        log.traces.forEach { trace ->
            val length = trace.events.size
            for (i in 1 until length) {
                val event1 = trace.events[i - 1]
                val event2 = trace.events[i]
                val pair = event1.activity to event2.activity
                successions.putIfAbsent(pair, 0)
                successions[pair]!!.inc()
            }
        }
        return successions
    }
}