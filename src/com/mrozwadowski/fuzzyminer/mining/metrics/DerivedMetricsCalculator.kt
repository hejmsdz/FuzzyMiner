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
        val derivedBinarySignificance = deriveBinaryMetrics(binarySignificance)
        val derivedBinaryCorrelation = deriveBinaryMetrics(binaryCorrelation)

        addMaps(aggregator.aggregateUnarySignificance, derivedUnarySignificance)
        addMaps(aggregator.aggregateBinarySignificance, derivedBinarySignificance)
        addMaps(aggregator.aggregateBinaryCorrelation, derivedBinaryCorrelation)
    }

    private fun deriveUnaryMetrics(metrics: Map<UnaryMetric, Double>): Map<XEventClass, Double> {
        val derived = mutableMapOf<XEventClass, Double>()
        metrics.keys.filterIsInstance<DerivedUnaryMetric>().forEach { metric ->
            val weight = metrics[metric] ?: return@forEach
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
            addMaps(derived, normalize(metric.values, weight))
        }
        return derived
    }

    private fun deriveBinaryMetrics(metrics: Map<BinaryMetric, Double>): Map<XEventClassPair, Double> {
        val derived = mutableMapOf<XEventClassPair, Double>()
        metrics.keys.filterIsInstance<DerivedBinaryMetric>().forEach { metric ->
            val weight = metrics[metric] ?: return@forEach
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
            addMaps(derived, normalize(metric.values, weight))
        }
        return derived
    }
}