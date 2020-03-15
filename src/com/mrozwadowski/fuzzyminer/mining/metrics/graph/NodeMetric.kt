package com.mrozwadowski.fuzzyminer.mining.metrics.graph

import com.mrozwadowski.fuzzyminer.data.graph.Node
import com.mrozwadowski.fuzzyminer.data.graph.PrimitiveNode
import com.mrozwadowski.fuzzyminer.mining.metrics.UnaryMetric

class NodeMetric(private val unaryMetric: UnaryMetric) {
    fun calculate(node: PrimitiveNode): Double {
        return unaryMetric.calculate(node.eventClass)
    }

    fun calculate(node: Node): Double {
        if (node is PrimitiveNode) {
            return calculate(node)
        }
        throw NotImplementedError()
    }
}