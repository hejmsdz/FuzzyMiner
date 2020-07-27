package com.mrozwadowski.fuzzyminer.mining.metrics

fun minimalMetrics(): MetricsStore = MetricsStore(mapOf(
    UnaryFrequency() to 1.0
), mapOf(
    BinaryFrequency() to 1.0
), mapOf(
    EndpointCorrelation() to 1.0
), null)
