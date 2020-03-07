package com.mrozwadowski.fuzzyminer.output

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import java.lang.StringBuilder

class Dot<EventClass>(private val graph: Graph<EventClass>) {
    override fun toString(): String {
        val out = StringBuilder()
        out.appendln("digraph {")
        graph.nodes.forEach { node ->
            out.appendln("  ${node.id} [label=\"${node}\"]")
        }
        graph.allEdges().forEach { (source, target) ->
            out.appendln("  ${source.id} -> ${target.id}")
        }
        out.appendln("}")
        return out.toString()
    }
}