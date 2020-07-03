package com.mrozwadowski.fuzzyminer.data.graph

data class Edge(val source: Node, val target: Node) {
    var significance: Double = 0.0
    var correlation: Double = 0.0
}