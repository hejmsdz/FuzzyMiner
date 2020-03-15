package com.mrozwadowski.fuzzyminer.mining.metrics.graph

import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.metrics.BinaryMetric

class EdgeMetric(private val binaryMetric: BinaryMetric) {
    fun calculate(node1: PrimitiveNode, node2: PrimitiveNode): Double {
        return binaryMetric.calculate(node1.eventClass, node2.eventClass)
    }

    fun calculate(node1: Node, node2: Node): Double {
        if (node1 is PrimitiveNode && node2 is PrimitiveNode) {
            return calculate(node1, node2)
        }
        throw NotImplementedError()
    }
}
