package com.mrozwadowski.fuzzyminer.mining.metrics

import com.mrozwadowski.fuzzyminer.mining.metrics.attenuation.Attenuation
import org.deckfour.xes.classification.XEventClass
import org.deckfour.xes.classification.XEventClasses
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog

interface Metric {
    fun reset()
}

typealias XEventClassPair = Pair<XEventClass, XEventClass>

abstract class UnaryMetric: Metric {
    val values = mutableMapOf<XEventClass, Double>()

    override fun reset() {
        values.clear()
    }
}

abstract class BinaryMetric: Metric {
    val values = mutableMapOf<XEventClassPair, Double>()

    override fun reset() {
        values.clear()
    }
}

abstract class LogBasedUnaryMetric: UnaryMetric() {
    abstract fun evaluate(event: XEvent): Double

    fun processEvent(event: XEvent, eventClass: XEventClass, factor: Double = 1.0) {
        values[eventClass] = values.getOrDefault(eventClass, 0.0) + evaluate(event) * factor
    }
}

abstract class LogBasedBinaryMetric(val normalize: Boolean): BinaryMetric() {
    abstract fun evaluate(previousEvent: XEvent, event: XEvent): Double

    fun processRelation(previousEvent: XEvent, previousEventClass: XEventClass, event: XEvent, eventClass: XEventClass, attenuation: Double) {
        val key = previousEventClass to eventClass
        values[key] = values.getOrDefault(key, 0.0) + evaluate(previousEvent, event) * attenuation
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
    private val unarySignificance: Map<UnaryMetric, Double>,
    private val binarySignificance: Map<BinaryMetric, Double>,
    private val binaryCorrelation: Map<BinaryMetric, Double>,
    private val attenuation: Attenuation?
) {
    private val metrics = unarySignificance.keys + binarySignificance.keys + binaryCorrelation.keys
    private val logBasedUnaryMetrics = metrics.filterIsInstance<LogBasedUnaryMetric>()
    private val logBasedBinaryMetrics = metrics.filterIsInstance<LogBasedBinaryMetric>()

    val normalizationFactors = mutableMapOf<XEventClassPair, Double>()

    val aggregateUnarySignificance = mutableMapOf<XEventClass, Double>()
    val aggregateBinarySignificance = mutableMapOf<XEventClassPair, Double>()
    val aggregateBinaryCorrelation = mutableMapOf<XEventClassPair, Double>()

    var logBasedUnarySignificanceWeight = 0.0
    var logBasedBinarySignificanceWeight = 0.0
    var logBasedBinaryCorrelationWeight = 0.0

    fun reset() {
//        metrics.forEach { it.reset() }
        aggregateUnarySignificance.clear()
        aggregateBinarySignificance.clear()
        aggregateBinaryCorrelation.clear()
//        normalizationFactors.clear()

        logBasedUnarySignificanceWeight = 0.0
        logBasedBinarySignificanceWeight = 0.0
        logBasedBinaryCorrelationWeight = 0.0
    }

    fun calculateFromLog(log: XLog, eventClasses: XEventClasses, factor: Double = 1.0) {
        val maxDistance = if (attenuation == null) 1 else attenuation.maxDistance

        log.forEach { trace ->
            trace.withIndex().forEach { (i, event) ->
                val eventClass = eventClasses.getClassOf(event)
                processEvent(event, eventClass, factor)

                val lookBack = minOf(maxDistance ?: i, i)
                for (distance in 1..lookBack) {
                    val previousEvent = trace[i - distance]
                    val previousEventClass = eventClasses.getClassOf(previousEvent)
                    processRelation(previousEvent, previousEventClass, event, eventClass, factor * (attenuation?.factor(distance) ?: 1.0))
                }
            }
        }
        calculateDerivedMetrics()
    }

    fun dumpMetrics(): MetricsDump {
        val unarySignificanceDump = unarySignificance.keys.flatMap { metric ->
            val metricName = metric.javaClass.simpleName
            metric.values.map { (eventClass, value) ->
                UnaryMetricEntry(metricName, eventClass.id) to value
            }
        }.toMap()

        val binarySignificanceDump = binarySignificance.keys.flatMap { metric ->
            val metricName = metric.javaClass.simpleName
            metric.values.map { (eventClasses, value) ->
                BinaryMetricEntry(metricName, eventClasses.first.id, eventClasses.second.id) to value
            }
        }.toMap()

        val binaryCorrelationDump = binaryCorrelation.keys.flatMap { metric ->
            val metricName = metric.javaClass.simpleName
            metric.values.map { (eventClasses, value) ->
                BinaryMetricEntry(metricName, eventClasses.first.id, eventClasses.second.id) to value
            }
        }.toMap()

        return MetricsDump(unarySignificanceDump, binarySignificanceDump, binaryCorrelationDump)
    }

    private fun processEvent(event: XEvent, eventClass: XEventClass, factor: Double) {
        logBasedUnaryMetrics.forEach { it.processEvent(event, eventClass, factor) }
    }

    private fun processRelation(previousEvent: XEvent, previousEventClass: XEventClass, event: XEvent, eventClass: XEventClass, attenuation: Double) {
        normalizationFactors.compute(previousEventClass to eventClass) { _, value -> (value ?: 0.0) + attenuation }
        logBasedBinaryMetrics.forEach { it.processRelation(previousEvent, previousEventClass, event, eventClass, attenuation) }
    }

    private fun calculateDerivedMetrics() {
        aggregateLogBasedMetrics()

        val derivedUnarySignificance = mutableMapOf<XEventClass, Double>()
        unarySignificance.keys.filterIsInstance<DerivedUnaryMetric>().forEach { metric ->
            val weight = unarySignificance[metric] ?: return@forEach
            metric.setLogBasedWeights(
                logBasedUnarySignificanceWeight,
                logBasedBinarySignificanceWeight,
                logBasedBinaryCorrelationWeight
            )
            metric.calculate(aggregateUnarySignificance, aggregateBinarySignificance, aggregateBinaryCorrelation)
            addMaps(derivedUnarySignificance, normalize(metric.values, weight))
        }
        addMaps(aggregateUnarySignificance, derivedUnarySignificance)

        val derivedBinarySignificance = mutableMapOf<XEventClassPair, Double>()
        binarySignificance.keys.filterIsInstance<DerivedBinaryMetric>().forEach { metric ->
            val weight = binarySignificance[metric] ?: return@forEach
            metric.setLogBasedWeights(
                logBasedUnarySignificanceWeight,
                logBasedBinarySignificanceWeight,
                logBasedBinaryCorrelationWeight
            )
            metric.calculate(aggregateUnarySignificance, aggregateBinarySignificance, aggregateBinaryCorrelation)
            addMaps(derivedBinarySignificance, normalize(metric.values, weight))
        }
        addMaps(aggregateBinarySignificance, derivedBinarySignificance)

        val derivedBinaryCorrelation = mutableMapOf<XEventClassPair, Double>()
        binaryCorrelation.keys.filterIsInstance<DerivedBinaryMetric>().forEach { metric ->
            val weight = binaryCorrelation[metric] ?: return@forEach
            metric.setLogBasedWeights(
                logBasedUnarySignificanceWeight,
                logBasedBinarySignificanceWeight,
                logBasedBinaryCorrelationWeight
            )
            metric.calculate(aggregateUnarySignificance, aggregateBinarySignificance, aggregateBinaryCorrelation)
            addMaps(derivedBinaryCorrelation, normalize(metric.values, weight))
        }
        addMaps(aggregateBinaryCorrelation, derivedBinaryCorrelation)
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
            val values = if (metric.normalize) {
                metric.values.mapValues { (key, value) -> value / normalizationFactors.getOrDefault(key, 1.0) }
            } else {
                metric.values
            }
            addMaps(aggregateBinarySignificance, normalize(values, weight))
        }

        binaryCorrelation.keys.filterIsInstance<LogBasedBinaryMetric>().forEach { metric ->
            val weight = binaryCorrelation[metric] ?: return@forEach
            logBasedBinaryCorrelationWeight += weight
            val values = if (metric.normalize) {
                metric.values.mapValues { (key, value) -> value / normalizationFactors.getOrDefault(key, 1.0) }
            } else {
                metric.values
            }
            addMaps(aggregateBinaryCorrelation, normalize(values, weight))
        }
    }

    private fun <K>normalize(values: Map<K, Double>, weight: Double): Map<K, Double> {
        val max = values.values.max() ?: return values
        return values.mapValues { weight * it.value / max }
    }

    fun <K>addMaps(target: MutableMap<K, Double>, addition: Map<K, Double>) {
        addition.forEach { (key, addValue) ->
            target.compute(key) { _, value -> (value ?: 0.0) + addValue }
        }
    }
}