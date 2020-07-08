package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.mining.metrics.attenuation.NRootAttenuation

fun defaultMetrics(): MetricsStore = MetricsStore(mapOf(
    UnaryFrequency() to 0.4,
    RoutingSignificance() to 0.6
), mapOf(
    BinaryFrequency() to 0.7,
    DistanceSignificance() to 0.3
), mapOf(
    EndpointCorrelation() to 0.4,
    OriginatorCorrelation() to 0.4,
    ProximityCorrelation() to 0.2
), NRootAttenuation(2.7, 5))
