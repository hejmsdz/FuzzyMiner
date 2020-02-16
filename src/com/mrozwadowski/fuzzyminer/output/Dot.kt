package com.mrozwadowski.fuzzyminer.output

import com.mrozwadowski.fuzzyminer.data.graph.Graph
import java.lang.StringBuilder

class Dot(private val graph: Graph) {
    override fun toString(): String {
        val out = StringBuilder()
        out.appendln("digraph {")
        graph.nodes.withIndex().forEach { indexed ->
            out.appendln("  ${indexed.index} [label=\"${indexed.value.name}\"]")
        }
        graph.edges.forEach { entry ->
            entry.value.forEach { edge ->
                out.appendln("  ${graph.nodes.indexOf(entry.key)} -> ${graph.nodes.indexOf(edge.target)}")
            }
        }
        out.appendln("}")
        return out.toString()
    }
}