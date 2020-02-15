package com.mrozwadowski.fuzzyminer.data.graph

data class Graph(val nodes: List<Node>, val edges: Map<Node, List<Edge>>)