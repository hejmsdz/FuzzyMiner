package com.mrozwadowski.fuzzyminer.data.graph

import org.deckfour.xes.classification.XEventClass

data class Node(val eventClass: XEventClass, val id: Int) {
    override fun toString() = eventClass.toString()
}
