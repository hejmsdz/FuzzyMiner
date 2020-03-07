package com.mrozwadowski.fuzzyminer.output

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import java.lang.StringBuilder

class Dot(private val graph: Graph) {
    override fun toString(): String {
        val out = StringBuilder()
        out.appendln("digraph {")
        graph.nodes.forEach { node ->
            out.appendln("  ${node.id} [label=\"${node.name}\"]")
        }
        graph.allEdges().forEach { (source, target) ->
            out.appendln("  ${source.id} -> ${target.id}")
        }
        out.appendln("}")
        return out.toString()
    }
}