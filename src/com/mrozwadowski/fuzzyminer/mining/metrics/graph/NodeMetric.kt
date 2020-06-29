package com.mrozwadowski.fuzzyminer.mining.metrics.graph

import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.metrics.UnaryMetric

class NodeMetric(private val unaryMetric: UnaryMetric) {
    fun calculate(node: Node): Double {
        if (node is PrimitiveNode) {
            return forNode(node)
        } else if (node is NodeCluster) {
            return forCluster(node)
        }
        throw NotImplementedError()
    }

    private fun forNode(node: PrimitiveNode): Double {
        return unaryMetric.calculate(node.eventClass)
    }

    private fun forCluster(cluster: NodeCluster): Double {
        return cluster.nodes.sumByDouble { calculate(it) }
    }
}