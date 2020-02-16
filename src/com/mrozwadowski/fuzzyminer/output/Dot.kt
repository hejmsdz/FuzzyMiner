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
        graph.edges.forEach { (sourceNode, edges) ->
            edges.forEach { edge ->
                out.appendln("  ${sourceNode.id} -> ${edge.target.id}")
            }
        }
        out.appendln("}")
        return out.toString()
    }
}