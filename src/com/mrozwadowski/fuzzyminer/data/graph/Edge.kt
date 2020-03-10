package com.mrozwadowski.fuzzyminer.data.graph

data class Edge<EventClass>(val source: Node<EventClass>, val target: Node<EventClass>)