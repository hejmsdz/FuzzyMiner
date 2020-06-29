package com.mrozwadowski.fuzzyminer.data.graph

interface Node {
    val id: Int
    override fun toString(): String

    fun toCluster(): NodeCluster
    operator fun plus(other: Node): NodeCluster = toCluster() + other.toCluster()
}