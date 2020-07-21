package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.classification.XEventClass

class DerivedMetricsCalculator(
    private val unarySignificance: Map<UnaryMetric, Double>,
    private val binarySignificance: Map<BinaryMetric, Double>,
    private val binaryCorrelation: Map<BinaryMetric, Double>,
    private val aggregator: MetricsAggregator
) {
    fun calculate() {
        val derivedUnarySignificance = deriveUnaryMetrics(unarySignificance)
        addMaps(aggregator.aggregateUnarySignificance, derivedUnarySignificance)

        val derivedBinarySignificance = deriveBinaryMetrics(binarySignificance)
        addMaps(aggregator.aggregateBinarySignificance, derivedBinarySignificance)

        val derivedBinaryCorrelation = deriveBinaryMetrics(binaryCorrelation)
        addMaps(aggregator.aggregateBinaryCorrelation, derivedBinaryCorrelation)
    }

    private fun deriveUnaryMetrics(metrics: Map<UnaryMetric, Double>): Map<XEventClass, Double> {
        val derived = mutableMapOf<XEventClass, Double>()
        metrics.keys.filterIsInstance<DerivedUnaryMetric>().forEach { metric ->
            val weight = metrics[metric] ?: return@forEach
            deriveMetric(metric, weight, derived)
        }
        return derived
    }

    private fun deriveBinaryMetrics(metrics: Map<BinaryMetric, Double>): Map<XEventClassPair, Double> {
        val derived = mutableMapOf<XEventClassPair, Double>()
        metrics.keys.filterIsInstance<DerivedBinaryMetric>().forEach { metric ->
            val weight = metrics[metric] ?: return@forEach
            deriveMetric(metric, weight, derived)
        }
        return derived
    }

    private fun <K>deriveMetric(metric: DerivedMetric<K>, weight: Double, target: MutableMap<K, Double>) {
        metric.setLogBasedWeights(
            aggregator.logBasedUnarySignificanceWeight,
            aggregator.logBasedBinarySignificanceWeight,
            aggregator.logBasedBinaryCorrelationWeight
        )
        metric.calculate(
            aggregator.aggregateUnarySignificance,
            aggregator.aggregateBinarySignificance,
            aggregator.aggregateBinaryCorrelation
        )
        addMaps(target, normalize(metric.values, weight))
    }
}