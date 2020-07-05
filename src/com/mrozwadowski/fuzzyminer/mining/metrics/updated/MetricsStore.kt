package com.mrozwadowski.fuzzyminer.mining.metrics.updated

import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.model.XEvent

interface Metric {}

abstract class UnaryMetric: Metric {
    val values = mutableMapOf<XEventClass, Double>()
}

abstract class BinaryMetric: Metric {
    val values = mutableMapOf<Pair<XEventClass, XEventClass>, Double>()
}

abstract class LogBasedUnaryMetric: UnaryMetric() {
    abstract fun evaluate(event: XEvent): Double

    fun processEvent(event: XEvent, eventClass: XEventClass) {
        values[eventClass] = values.getOrDefault(eventClass, 0.0) + evaluate(event)
    }
}

abstract class LogBasedBinaryMetric: BinaryMetric() {
    abstract fun evaluate(previousEvent: XEvent, event: XEvent): Double

    fun processRelation(previousEvent: XEvent, previousEventClass: XEventClass, event: XEvent, eventClass: XEventClass, attenuation: Double) {
        val key = previousEventClass to eventClass
        values[key] = values.getOrDefault(key, 0.0) + evaluate(previousEvent, event) * attenuation
    }
}

abstract class DerivedUnaryMetric: UnaryMetric() {
    abstract fun calculate(unarySignificance: Map<XEventClass, Double>, binarySignificance: Map<Pair<XEventClass, XEventClass>, Double>, binaryCorrelation: Map<Pair<XEventClass, XEventClass>, Double>)
}

abstract class DerivedBinaryMetric: BinaryMetric() {
    abstract fun calculate(unarySignificance: Map<XEventClass, Double>, binarySignificance: Map<Pair<XEventClass, XEventClass>, Double>, binaryCorrelation: Map<Pair<XEventClass, XEventClass>, Double>)
}

class MetricsStore(
    private val unarySignificance: Map<Metric, Double>,
    private val binarySignificance: Map<Metric, Double>,
    private val binaryCorrelation: Map<Metric, Double>
) {

    private val metrics = unarySignificance.keys + binarySignificance.keys + binaryCorrelation.keys
    private val logBasedUnaryMetrics = metrics.filterIsInstance<LogBasedUnaryMetric>()
    private val logBasedBinaryMetrics = metrics.filterIsInstance<LogBasedBinaryMetric>()

    val aggregateUnarySignificance = mutableMapOf<XEventClass, Double>()
    val aggregateBinarySignificance = mutableMapOf<Pair<XEventClass, XEventClass>, Double>()
    val aggregateBinaryCorrelation = mutableMapOf<Pair<XEventClass, XEventClass>, Double>()

    fun processEvent(event: XEvent, eventClass: XEventClass) {
        logBasedUnaryMetrics.forEach { it.processEvent(event, eventClass) }
    }

    fun processRelation(previousEvent: XEvent, previousEventClass: XEventClass, event: XEvent, eventClass: XEventClass, attenuation: Double) {
        logBasedBinaryMetrics.forEach { it.processRelation(previousEvent, previousEventClass, event, eventClass, attenuation) }
    }

    fun calculateDerivedMetrics() {
        aggregateLogBasedMetrics()

        unarySignificance.keys.filterIsInstance<DerivedUnaryMetric>().forEach { metric ->
            val weight = unarySignificance[metric] ?: return@forEach
            metric.calculate(aggregateUnarySignificance, aggregateBinarySignificance, aggregateBinaryCorrelation)
            addMaps(aggregateUnarySignificance, normalize(metric.values, weight))
        }

        binarySignificance.keys.filterIsInstance<DerivedBinaryMetric>().forEach { metric ->
            val weight = binarySignificance[metric] ?: return@forEach
            metric.calculate(aggregateUnarySignificance, aggregateBinarySignificance, aggregateBinaryCorrelation)
            addMaps(aggregateBinarySignificance, normalize(metric.values, weight))
        }

        binaryCorrelation.keys.filterIsInstance<DerivedBinaryMetric>().forEach { metric ->
            val weight = binaryCorrelation[metric] ?: return@forEach
            metric.calculate(aggregateUnarySignificance, aggregateBinarySignificance, aggregateBinaryCorrelation)
            addMaps(aggregateBinaryCorrelation, normalize(metric.values, weight))
        }
    }

    private fun aggregateLogBasedMetrics() {
        unarySignificance.keys.filterIsInstance<LogBasedUnaryMetric>().forEach { metric ->
            val weight = unarySignificance[metric] ?: return@forEach
            addMaps(aggregateUnarySignificance, normalize(metric.values, weight))
        }

        binarySignificance.keys.filterIsInstance<LogBasedBinaryMetric>().forEach { metric ->
            val weight = binarySignificance[metric] ?: return@forEach
            addMaps(aggregateBinarySignificance, normalize(metric.values, weight))
        }

        binaryCorrelation.keys.filterIsInstance<LogBasedBinaryMetric>().forEach { metric ->
            val weight = binaryCorrelation[metric] ?: return@forEach
            addMaps(aggregateBinaryCorrelation, normalize(metric.values, weight))
        }
    }

    private fun <K>normalize(values: Map<K, Double>, weight: Double): Map<K, Double> {
        val max = values.values.max() ?: return values
        return values.mapValues { weight * it.value / max }
    }

    private fun <K>addMaps(target: MutableMap<K, Double>, addition: Map<K, Double>) {
        addition.forEach { (key, addValue) ->
            target.compute(key) { _, value -> (value ?: 0.0) + addValue }
        }
    }

}