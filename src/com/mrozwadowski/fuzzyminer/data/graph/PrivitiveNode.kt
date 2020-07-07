package com.mrozwadowski.fuzzyminer.data.graph

import org.deckfour.xes.classification.XEventClass

data class PrimitiveNode(val eventClass: XEventClass, override var significance: Double): Node {
    constructor(eventClass: XEventClass): this(eventClass, 0.0)

    override val id = eventClass.index
    override fun toString() = eventClass.toString()

    override fun toCluster(): NodeCluster = NodeCluster(listOf(this))
}
