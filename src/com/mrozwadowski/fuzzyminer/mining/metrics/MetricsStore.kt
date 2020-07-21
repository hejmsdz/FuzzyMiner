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

interface DerivedMetric<K> {
    val values: Map<K, Double>

    var logBasedUnarySignificanceWeight: Double
    var logBasedBinarySignificanceWeight: Double
    var logBasedBinaryCorrelationWeight: Double

    fun setLogBasedWeights(unarySignificance: Double, binarySignificance: Double, binaryCorrelation: Double) {
        logBasedUnarySignificanceWeight = unarySignificance
        logBasedBinarySignificanceWeight = binarySignificance
        logBasedBinaryCorrelationWeight = binaryCorrelation
    }

    fun calculate(unarySignificance: Map<XEventClass, Double>, binarySignificance: Map<XEventClassPair, Double>, binaryCorrelation: Map<XEventClassPair, Double>)
}

abstract class DerivedUnaryMetric: UnaryMetric(), DerivedMetric<XEventClass> {
    override var logBasedUnarySignificanceWeight = 0.0
    override var logBasedBinarySignificanceWeight = 0.0
    override var logBasedBinaryCorrelationWeight = 0.0
}

abstract class DerivedBinaryMetric: BinaryMetric(), DerivedMetric<XEventClassPair> {
    override var logBasedUnarySignificanceWeight = 0.0
    override var logBasedBinarySignificanceWeight = 0.0
    override var logBasedBinaryCorrelationWeight = 0.0
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

    var aggregateUnarySignificance = mutableMapOf<XEventClass, Double>()
    var aggregateBinarySignificance = mutableMapOf<XEventClassPair, Double>()
    var aggregateBinaryCorrelation = mutableMapOf<XEventClassPair, Double>()

    private val aggregator = MetricsAggregator(unarySignificance, binarySignificance, binaryCorrelation, normalizationFactors)

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
        aggregator.aggregate()

        val derivedCalculator = DerivedMetricsCalculator(unarySignificance, binarySignificance, binaryCorrelation, aggregator)
        derivedCalculator.calculate()

        aggregateUnarySignificance = aggregator.aggregateUnarySignificance
        aggregateBinarySignificance = aggregator.aggregateBinarySignificance
        aggregateBinaryCorrelation = aggregator.aggregateBinaryCorrelation
    }

    fun reset() {
        aggregator.reset()
    }
}

fun <K>normalize(values: Map<K, Double>, weight: Double): Map<K, Double> {
    val max = values.values.max() ?: return values
    return values.mapValues { weight * it.value / max }
}

fun <K>addMaps(target: MutableMap<K, Double>, addition: Map<K, Double>) {
    addition.forEach { (key, addValue) ->
        target.compute(key) { _, value -> (value ?: 0.0) + addValue }
    }
}
