package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog

interface Metric {}

typealias XEventClassPair = Pair<XEventClass, XEventClass>

abstract class UnaryMetric: Metric {
    val values = mutableMapOf<XEventClass, Double>()
}

abstract class BinaryMetric: Metric {
    val values = mutableMapOf<XEventClassPair, Double>()
}

abstract class LogBasedUnaryMetric: UnaryMetric() {
    abstract fun evaluate(event: XEvent): Double

    fun processEvent(event: XEvent, eventClass: XEventClass) {
        values[eventClass] = values.getOrDefault(eventClass, 0.0) + evaluate(event)
    }
}

abstract class LogBasedBinaryMetric(private val normalize: Boolean): BinaryMetric() {
    private val normalizationFactors = mutableMapOf<XEventClassPair, Double>()

    abstract fun evaluate(previousEvent: XEvent, event: XEvent): Double

    fun processRelation(previousEvent: XEvent, previousEventClass: XEventClass, event: XEvent, eventClass: XEventClass, attenuation: Double) {
        val key = previousEventClass to eventClass
        values[key] = values.getOrDefault(key, 0.0) + evaluate(previousEvent, event) * attenuation

        if (normalize) {
            normalizationFactors.compute(key) { _, value -> (value ?: 0.0) + attenuation }
        }
    }

    fun applyNormalization() {
        if (normalize) {
            values.mapValuesTo(values) { (key, value) -> value / normalizationFactors.getOrDefault(key, 1.0) }
        }
    }
}

abstract class DerivedUnaryMetric: UnaryMetric() {
    var logBasedUnarySignificanceWeight = 0.0
    var logBasedBinarySignificanceWeight = 0.0
    var logBasedBinaryCorrelationWeight = 0.0

    fun setLogBasedWeights(unarySignificance: Double, binarySignificance: Double, binaryCorrelation: Double) {
        logBasedUnarySignificanceWeight = unarySignificance
        logBasedBinarySignificanceWeight = binarySignificance
        logBasedBinaryCorrelationWeight = binaryCorrelation
    }

    abstract fun calculate(unarySignificance: Map<XEventClass, Double>, binarySignificance: Map<XEventClassPair, Double>, binaryCorrelation: Map<XEventClassPair, Double>)
}

abstract class DerivedBinaryMetric: BinaryMetric() {
    var logBasedUnarySignificanceWeight = 0.0
    var logBasedBinarySignificanceWeight = 0.0
    var logBasedBinaryCorrelationWeight = 0.0

    fun setLogBasedWeights(unarySignificance: Double, binarySignificance: Double, binaryCorrelation: Double) {
        logBasedUnarySignificanceWeight = unarySignificance
        logBasedBinarySignificanceWeight = binarySignificance
        logBasedBinaryCorrelationWeight = binaryCorrelation
    }

    abstract fun calculate(unarySignificance: Map<XEventClass, Double>, binarySignificance: Map<XEventClassPair, Double>, binaryCorrelation: Map<XEventClassPair, Double>)
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
    val aggregateBinarySignificance = mutableMapOf<XEventClassPair, Double>()
    val aggregateBinaryCorrelation = mutableMapOf<XEventClassPair, Double>()

    var logBasedUnarySignificanceWeight = 0.0
    var logBasedBinarySignificanceWeight = 0.0
    var logBasedBinaryCorrelationWeight = 0.0

    fun calculateFromLog(log: XLog, eventClasses: XEventClasses) {
        log.forEach { trace ->
            trace.withIndex().forEach { (i, event) ->
                val eventClass = eventClasses.getClassOf(event)
                processEvent(event, eventClass)

                if (i >= 1) {
                    val previousEvent = trace[i - 1]
                    val previousEventClass = eventClasses.getClassOf(previousEvent)
                    processRelation(previousEvent, previousEventClass, event, eventClass, 1.0)
                }
            }
        }
        calculateDerivedMetrics()
    }

    private fun processEvent(event: XEvent, eventClass: XEventClass) {
        logBasedUnaryMetrics.forEach { it.processEvent(event, eventClass) }
    }

    private fun processRelation(previousEvent: XEvent, previousEventClass: XEventClass, event: XEvent, eventClass: XEventClass, attenuation: Double) {
        logBasedBinaryMetrics.forEach { it.processRelation(previousEvent, previousEventClass, event, eventClass, attenuation) }
    }

    private fun calculateDerivedMetrics() {
        aggregateLogBasedMetrics()


        unarySignificance.keys.filterIsInstance<DerivedUnaryMetric>().forEach { metric ->
            val weight = unarySignificance[metric] ?: return@forEach
            metric.setLogBasedWeights(logBasedUnarySignificanceWeight, logBasedBinarySignificanceWeight, logBasedBinaryCorrelationWeight)
            metric.calculate(aggregateUnarySignificance, aggregateBinarySignificance, aggregateBinaryCorrelation)
            addMaps(aggregateUnarySignificance, normalize(metric.values, weight))
        }

        binarySignificance.keys.filterIsInstance<DerivedBinaryMetric>().forEach { metric ->
            val weight = binarySignificance[metric] ?: return@forEach
            metric.setLogBasedWeights(logBasedUnarySignificanceWeight, logBasedBinarySignificanceWeight, logBasedBinaryCorrelationWeight)
            metric.calculate(aggregateUnarySignificance, aggregateBinarySignificance, aggregateBinaryCorrelation)
            addMaps(aggregateBinarySignificance, normalize(metric.values, weight))
        }

        binaryCorrelation.keys.filterIsInstance<DerivedBinaryMetric>().forEach { metric ->
            val weight = binaryCorrelation[metric] ?: return@forEach
            metric.setLogBasedWeights(logBasedUnarySignificanceWeight, logBasedBinarySignificanceWeight, logBasedBinaryCorrelationWeight)
            metric.calculate(aggregateUnarySignificance, aggregateBinarySignificance, aggregateBinaryCorrelation)
            addMaps(aggregateBinaryCorrelation, normalize(metric.values, weight))
        }
    }

    private fun aggregateLogBasedMetrics() {
        unarySignificance.keys.filterIsInstance<LogBasedUnaryMetric>().forEach { metric ->
            val weight = unarySignificance[metric] ?: return@forEach
            logBasedUnarySignificanceWeight += weight
            addMaps(aggregateUnarySignificance, normalize(metric.values, weight))
        }

        binarySignificance.keys.filterIsInstance<LogBasedBinaryMetric>().forEach { metric ->
            val weight = binarySignificance[metric] ?: return@forEach
            logBasedBinarySignificanceWeight += weight
            metric.applyNormalization()
            addMaps(aggregateBinarySignificance, normalize(metric.values, weight))
        }

        binaryCorrelation.keys.filterIsInstance<LogBasedBinaryMetric>().forEach { metric ->
            val weight = binaryCorrelation[metric] ?: return@forEach
            logBasedBinaryCorrelationWeight += weight
            metric.applyNormalization()
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