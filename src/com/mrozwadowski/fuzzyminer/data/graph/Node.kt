package com.mrozwadowski.fuzzyminer.data.graph

data class Node<EventClass>(val eventClass: EventClass, val id: Int) {
    override fun toString() = eventClass.toString()
}