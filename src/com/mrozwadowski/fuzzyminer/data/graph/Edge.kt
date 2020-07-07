package com.mrozwadowski.fuzzyminer.data.graph

data class Edge(val source: Node, val target: Node, val significance: Double, val correlation: Double) {
    constructor(source: Node, target: Node): this(source, target, 0.0, 0.0)
}