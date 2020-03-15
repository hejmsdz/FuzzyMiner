package com.mrozwadowski.fuzzyminer.mining.metrics.graph

import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.NodeCluster
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryMetric

class EdgeMetric(private val binaryMetric: BinaryMetric) {
    fun calculate(node1: Node, node2: Node): Double {
        if (node1 is PrimitiveNode && node2 is PrimitiveNode) {
            return nodeToNode(node1, node2)
        }
        if (node1 is PrimitiveNode && node2 is NodeCluster) {
            return nodeToCluster(node1, node2)
        }
        if (node1 is NodeCluster && node2 is NodeCluster) {
            return clusterToCluster(node1, node2)
        }
        throw NotImplementedError()
    }

    private fun nodeToNode(node1: PrimitiveNode, node2: PrimitiveNode): Double {
        return binaryMetric.calculate(node1.eventClass, node2.eventClass)
    }

    private fun nodeToCluster(node: PrimitiveNode, cluster: NodeCluster): Double {
        return cluster.nodes.map { calculate(node, it) }.min() ?: 0.0
    }

    private fun clusterToCluster(cluster1: NodeCluster, cluster2: NodeCluster): Double {
        return cluster1.nodes.map { calculate(cluster2, it) }.min() ?: 0.0
    }
}
