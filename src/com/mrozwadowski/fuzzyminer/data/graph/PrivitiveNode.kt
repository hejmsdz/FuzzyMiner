package com.mrozwadowski.fuzzyminer.data.graph

import org.deckfour.xes.classification.XEventClass

data class PrimitiveNode(val eventClass: XEventClass): Node {
    override val id = eventClass.index
    override var significance = 0.0
    override fun toString() = eventClass.toString()

    override fun toCluster(): NodeCluster = NodeCluster(listOf(this))
}
