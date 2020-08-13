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
            val weight = unarySignificance[it] ?: return@forEach
            logBasedUnarySignificanceWeight += weight
            aggregateUnaryMetric(it, weight, aggregateUnarySignificance)
        }
        aggregateUnarySignificance.entries.retainAll { significantlyGreater(it.value, 0.0) }
        binarySignificance.keys.filterIsInstance<LogBasedBinaryMetric>().forEach {
            val weight = binarySignificance[it] ?: return@forEach
            logBasedBinarySignificanceWeight += weight
            aggregateBinaryMetric(it, weight, aggregateBinarySignificance)
        }
        aggregateBinarySignificance.entries.retainAll { significantlyGreater(it.value, 0.0) }
        binaryCorrelation.keys.filterIsInstance<LogBasedBinaryMetric>().forEach {
            val weight = binaryCorrelation[it] ?: return@forEach
            logBasedBinaryCorrelationWeight += weight
            aggregateBinaryMetric(it, weight, aggregateBinaryCorrelation)
        }
        aggregateBinaryCorrelation.entries.retainAll { significantlyGreater(it.value, 0.0) }
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
        addMaps(target, normalize(metric.values, weight))
    }

    private fun aggregateBinaryMetric(metric: LogBasedBinaryMetric, weight: Double, target: MutableMap<XEventClassPair, Double>) {
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