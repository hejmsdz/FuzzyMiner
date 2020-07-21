package com.mrozwadowski.fuzzyminer.mining.metrics

data class MetricsDump(
    val unarySignificance: Map<UnaryMetricEntry, Double>,
    val binarySignificance: Map<BinaryMetricEntry, Double>,
    val binaryCorrelation: Map<BinaryMetricEntry, Double>
)

data class UnaryMetricEntry(
    val metricName: String,
    val eventClassName: String
) {
    override fun toString(): String = "($metricName; $eventClassName)"
}

data class BinaryMetricEntry(
    val metricName: String,
    val eventClass1Name: String,
    val eventClass2Name: String
) {
    override fun toString(): String = "($metricName; $eventClass1Name:$eventClass2Name)"
}
