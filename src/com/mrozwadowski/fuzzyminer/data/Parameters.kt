package com.mrozwadowski.fuzzyminer.data

data class Parameters(
    val preserveThreshold: Double,
    val ratioThreshold: Double,
    val utilityRatio: Double,
    val edgeCutoff: Double,
    val nodeCutoff: Double
)