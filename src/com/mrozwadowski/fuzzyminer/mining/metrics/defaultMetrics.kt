package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.mining.metrics.attenuation.NRootAttenuation

fun defaultMetrics(): MetricsStore = MetricsStore(mapOf(
    UnaryFrequency() to 0.5,
    RoutingSignificance() to 0.5
), mapOf(
    BinaryFrequency() to 0.5,
    DistanceSignificance() to 0.5
), mapOf(
    EndpointCorrelation() to 0.3,
    OriginatorCorrelation() to 0.3,
    ProximityCorrelation() to 0.4
), NRootAttenuation(2.7, 4))
