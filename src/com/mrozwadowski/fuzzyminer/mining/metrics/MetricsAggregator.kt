package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.utils.significantlyGreater
import org.deckfour.xes.classification.XEventClass

class MetricsAggregator(
    private val unarySignificance: Map<UnaryMetric, Double>,
    private val binarySignificance: Map<BinaryMetric, Double>,
    private val binaryCorrelation: Map<BinaryMetric, Double>,
    private val normalizationFactors: Map<XEventClassPair, Double>
) {
    var logBasedUnarySignificanceWeight = 0.0
    var logBasedBinarySignificanceWeight = 0.0
    var logBasedBinaryCorrelationWeight = 0.0

    val aggregateUnarySignificance = mutableMapOf<XEventClass, Double>()
    val aggregateBinarySignificance = mutableMapOf<XEventClassPair, Double>()
    val aggregateBinaryCorrelation = mutableMapOf<XEventClassPair, Double>()

    fun aggregate() {
        unarySignificance.keys.filterIsInstance<LogBasedUnaryMetric>().forEach {
            aggregateUnaryMetric(it, unarySignificance[it] ?: 0.0, aggregateUnarySignificance)
        }
        binarySignificance.keys.filterIsInstance<LogBasedBinaryMetric>().forEach {
            aggregateBinaryMetric(it, binarySignificance[it] ?: 0.0, aggregateBinarySignificance)
        }
        binaryCorrelation.keys.filterIsInstance<LogBasedBinaryMetric>().forEach {
            aggregateBinaryMetric(it, binaryCorrelation[it] ?: 0.0, aggregateBinaryCorrelation)
        }
    }

    fun reset() {
        aggregateUnarySignificance.clear()
        aggregateBinarySignificance.clear()
        aggregateBinaryCorrelation.clear()

        logBasedUnarySignificanceWeight = 0.0
        logBasedBinarySignificanceWeight = 0.0
        logBasedBinaryCorrelationWeight = 0.0
    }

    private fun aggregateUnaryMetric(metric: LogBasedUnaryMetric, weight: Double, target: MutableMap<XEventClass, Double>) {
        logBasedUnarySignificanceWeight += weight
        addMaps(target, normalize(metric.values, weight))
    }

    private fun aggregateBinaryMetric(metric: LogBasedBinaryMetric, weight: Double, target: MutableMap<XEventClassPair, Double>) {
        logBasedBinarySignificanceWeight += weight
        val values = if (metric.normalize) {
            metric.values.mapValues { (key, value) ->
                val normalizationFactor = normalizationFactors.getOrDefault(key, 1.0)
                if (significantlyGreater(normalizationFactor, 0.0)) {
                    value / normalizationFactor
                } else {
                    0.0
                }
            }
        } else {
            metric.values
        }
        val normalized = normalize(values, weight)
        addMaps(target, normalized)
    }
}