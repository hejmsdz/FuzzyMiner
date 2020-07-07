package com.mrozwadowski.fuzzyminer.mining.metrics

fun defaultMetrics(): MetricsStore = MetricsStore(mapOf(
    UnaryFrequency() to 0.4,
    RoutingSignificance() to 0.6
), mapOf(
    BinaryFrequency() to 0.7,
    DistanceSignificance() to 0.3
), mapOf(
    EndpointCorrelation() to 1.0
))